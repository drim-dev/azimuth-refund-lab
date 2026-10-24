package dev.drim.azimuth.refund.worker;

import dev.drim.azimuth.Azimuth.ImplementsMechanism;
import dev.drim.azimuth.Azimuth.Realizes;
import dev.drim.azimuth.refund.contracts.event.RefundMessaging;
import dev.drim.azimuth.refund.contracts.event.RefundResultV1;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
class RefundResultPublisher {
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final Duration confirmTimeout;

    RefundResultPublisher(
            RabbitTemplate rabbitTemplate,
            ObjectMapper objectMapper,
            @Value("${refund.messaging.confirm-timeout}") Duration confirmTimeout
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.confirmTimeout = confirmTimeout;
    }

    @Realizes(spec = "refunds/execution", scenario = "interrupted-processing-eventually-completes")
    @ImplementsMechanism(spec = "refunds/execution", mechanism = "confirmed-refund-result")
    void publishConfirmed(RefundResultV1 result) {
        MessageProperties properties = new MessageProperties();
        properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        properties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        properties.setMessageId(result.eventId().toString());
        properties.setType(result.eventType());

        try {
            Message message = new Message(objectMapper.writeValueAsBytes(result), properties);
            CorrelationData correlation = new CorrelationData(result.eventId().toString());
            rabbitTemplate.send(
                    RefundMessaging.EXCHANGE,
                    RefundMessaging.RESULT_ROUTING_KEY,
                    message,
                    correlation);

            CorrelationData.Confirm confirm = correlation.getFuture().get(
                    confirmTimeout.toMillis(), TimeUnit.MILLISECONDS);
            if (!confirm.isAck()) {
                throw new ResultPublicationFailedException(
                        "Broker rejected refund result: " + confirm.getReason());
            }
            if (correlation.getReturned() != null) {
                throw new ResultPublicationFailedException(
                        "Broker returned unroutable refund result: "
                                + correlation.getReturned().getReplyText());
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ResultPublicationFailedException("Refund result confirmation was interrupted", exception);
        } catch (ExecutionException | TimeoutException exception) {
            throw new ResultPublicationFailedException("Refund result was not confirmed", exception);
        }
    }

    static final class ResultPublicationFailedException extends RuntimeException {
        ResultPublicationFailedException(String message) {
            super(message);
        }

        ResultPublicationFailedException(String message, Exception cause) {
            super(message, cause);
        }
    }
}

package dev.drim.azimuth.refund.worker;

import com.rabbitmq.client.Channel;
import dev.drim.azimuth.refund.contracts.event.RefundEventNames;
import dev.drim.azimuth.refund.contracts.event.RefundMessaging;
import dev.drim.azimuth.refund.contracts.event.RefundRequestedV1;
import dev.drim.azimuth.refund.contracts.event.RefundResultV1;
import dev.drim.azimuth.refund.contracts.http.ProviderRefundResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import static dev.drim.azimuth.Azimuth.Realizes;

@Component
class RefundRequestedConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(RefundRequestedConsumer.class);

    private final ObjectMapper objectMapper;
    private final ProviderClient providerClient;
    private final DeliveryBoundary deliveryBoundary;
    private final RefundResultPublisher resultPublisher;

    RefundRequestedConsumer(
            ObjectMapper objectMapper,
            ProviderClient providerClient,
            DeliveryBoundary deliveryBoundary,
            RefundResultPublisher resultPublisher
    ) {
        this.objectMapper = objectMapper;
        this.providerClient = providerClient;
        this.deliveryBoundary = deliveryBoundary;
        this.resultPublisher = resultPublisher;
    }

    @RabbitListener(queues = RefundMessaging.WORKER_QUEUE, ackMode = "MANUAL")
    @Realizes(spec = "refunds/execution", scenario = "interrupted-processing-eventually-completes")
    public void consume(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            RefundRequestedV1 requested = objectMapper.readValue(
                    message.getBody(), RefundRequestedV1.class);
            ProviderRefundResponse providerResult = providerClient.apply(requested);
            deliveryBoundary.afterProviderResponse(requested.refundId());
            resultPublisher.publishConfirmed(toDeterministicResult(requested, providerResult));
            channel.basicAck(deliveryTag, false);
        } catch (DeliveryRestartRequested exception) {
            LOG.info("Demo failpoint requested refund delivery redelivery");
            channel.basicNack(deliveryTag, false, true);
        } catch (Exception exception) {
            LOG.warn("Refund request delivery will be retried", exception);
            channel.basicNack(deliveryTag, false, true);
        }
    }

    private static RefundResultV1 toDeterministicResult(
            RefundRequestedV1 requested,
            ProviderRefundResponse providerResult
    ) {
        UUID eventId = UUID.nameUUIDFromBytes(
                (RefundEventNames.REFUND_RESULT_V1 + ":" + requested.refundId())
                        .getBytes(StandardCharsets.UTF_8));
        return new RefundResultV1(
                eventId,
                RefundEventNames.REFUND_RESULT_V1,
                RefundEventNames.VERSION_1,
                providerResult.appliedAt(),
                requested.refundId(),
                providerResult.providerOperationId(),
                providerResult.outcome(),
                providerResult.appliedAt());
    }
}

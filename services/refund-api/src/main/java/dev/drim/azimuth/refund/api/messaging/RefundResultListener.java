package dev.drim.azimuth.refund.api.messaging;

import dev.drim.azimuth.refund.contracts.event.RefundMessaging;
import dev.drim.azimuth.refund.contracts.event.RefundResultV1;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class RefundResultListener {
    private final ObjectMapper objectMapper;
    private final RefundResultProcessor processor;

    public RefundResultListener(ObjectMapper objectMapper, RefundResultProcessor processor) {
        this.objectMapper = objectMapper;
        this.processor = processor;
    }

    @RabbitListener(queues = RefundMessaging.API_RESULT_QUEUE)
    public void consume(Message message) {
        try {
            processor.process(objectMapper.readValue(message.getBody(), RefundResultV1.class));
        } catch (JacksonException exception) {
            throw new IllegalArgumentException("Refund result event could not be read", exception);
        }
    }
}

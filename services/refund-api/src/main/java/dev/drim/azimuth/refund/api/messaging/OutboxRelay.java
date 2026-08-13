package dev.drim.azimuth.refund.api.messaging;

import dev.drim.azimuth.refund.contracts.event.RefundMessaging;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxRelay {
    private final JdbcTemplate jdbc;
    private final RabbitTemplate rabbit;
    private final Duration confirmTimeout;

    public OutboxRelay(
            JdbcTemplate jdbc,
            RabbitTemplate rabbit,
            @Value("${refund.outbox.confirm-timeout:5s}") Duration confirmTimeout
    ) {
        this.jdbc = jdbc;
        this.rabbit = rabbit;
        this.confirmTimeout = confirmTimeout;
        this.rabbit.setMandatory(true);
    }

    @Scheduled(fixedDelayString = "${refund.outbox.relay-delay:250ms}")
    public void relay() {
        List<OutboxMessage> messages = jdbc.query("""
                SELECT event_id, payload::text
                FROM refund_request_outbox
                WHERE published_at IS NULL
                ORDER BY created_at
                LIMIT 50
                """, this::mapMessage);
        for (OutboxMessage message : messages) {
            publish(message);
        }
    }

    private void publish(OutboxMessage outboxMessage) {
        MessageProperties properties = new MessageProperties();
        properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        properties.setMessageId(outboxMessage.eventId().toString());
        properties.setType(RefundMessaging.REQUESTED_ROUTING_KEY);
        Message message = new Message(outboxMessage.payload().getBytes(StandardCharsets.UTF_8), properties);
        CorrelationData correlation = new CorrelationData(outboxMessage.eventId().toString());
        rabbit.send(RefundMessaging.EXCHANGE, RefundMessaging.REQUESTED_ROUTING_KEY, message, correlation);
        try {
            CorrelationData.Confirm confirm = correlation.getFuture()
                    .get(confirmTimeout.toMillis(), TimeUnit.MILLISECONDS);
            if (!confirm.ack()) {
                throw new IllegalStateException("Broker rejected refund request: " + confirm.reason());
            }
            jdbc.update("""
                    UPDATE refund_request_outbox
                    SET published_at = ?
                    WHERE event_id = ? AND published_at IS NULL
                    """, Timestamp.from(Instant.now()), outboxMessage.eventId());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while confirming refund request", exception);
        } catch (Exception exception) {
            throw new IllegalStateException("Refund request publication was not confirmed", exception);
        }
    }

    private OutboxMessage mapMessage(ResultSet row, int rowNumber) throws SQLException {
        return new OutboxMessage(row.getObject("event_id", UUID.class), row.getString("payload"));
    }

    private record OutboxMessage(UUID eventId, String payload) {
    }
}

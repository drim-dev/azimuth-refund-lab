package dev.drim.azimuth.refund.contracts.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RefundRequestedV1(
        UUID eventId,
        String eventType,
        int eventVersion,
        Instant occurredAt,
        UUID refundId,
        UUID paymentId,
        BigDecimal amount,
        String currency,
        String approvedBy
) {
}

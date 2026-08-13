package dev.drim.azimuth.refund.contracts.event;

import java.time.Instant;
import java.util.UUID;

public record RefundResultV1(
        UUID eventId,
        String eventType,
        int eventVersion,
        Instant occurredAt,
        UUID refundId,
        UUID providerOperationId,
        RefundOutcome outcome,
        Instant appliedAt
) {
}

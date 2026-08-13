package dev.drim.azimuth.refund.contracts.http;

import dev.drim.azimuth.refund.contracts.event.RefundOutcome;
import java.time.Instant;
import java.util.UUID;

public record ProviderRefundResponse(
        UUID refundId,
        UUID providerOperationId,
        RefundOutcome outcome,
        Instant appliedAt
) {
}

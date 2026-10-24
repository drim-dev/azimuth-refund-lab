package dev.drim.azimuth.refund.contracts.http;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProviderRefundAttempt(
        String idempotencyKey,
        UUID refundId,
        UUID paymentId,
        BigDecimal amount,
        String currency,
        Instant attemptedAt
) {
}

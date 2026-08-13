package dev.drim.azimuth.refund.provider;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

record ProviderFinancialEffect(
        UUID effectId,
        UUID refundId,
        UUID paymentId,
        BigDecimal amount,
        String currency,
        Instant appliedAt
) {
    static ProviderFinancialEffect from(ProviderLedgerEntry refund, Instant appliedAt) {
        return new ProviderFinancialEffect(
                UUID.randomUUID(),
                refund.refundId(),
                refund.paymentId(),
                refund.amount(),
                refund.currency(),
                appliedAt);
    }
}

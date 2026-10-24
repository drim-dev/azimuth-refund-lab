package dev.drim.azimuth.refund.provider;

import dev.drim.azimuth.Azimuth.Realizes;
import dev.drim.azimuth.refund.contracts.event.RefundOutcome;
import dev.drim.azimuth.refund.contracts.http.ProviderRefundRequest;
import dev.drim.azimuth.refund.contracts.http.ProviderRefundResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

record ProviderLedgerEntry(
        String idempotencyKey,
        UUID refundId,
        UUID paymentId,
        BigDecimal amount,
        String currency,
        UUID providerOperationId,
        String outcome,
        Instant appliedAt
) {
    @Realizes(spec = "refunds/execution", scenario = "repeated-processing-does-not-repeat-refund")
    boolean matches(ProviderRefundRequest request) {
        return refundId.equals(request.refundId())
                && paymentId.equals(request.paymentId())
                && amount.compareTo(request.amount()) == 0
                && currency.equals(request.currency());
    }

    ProviderRefundResponse toResponse() {
        return new ProviderRefundResponse(
                idempotencyKey,
                refundId,
                providerOperationId,
                RefundOutcome.fromWireName(outcome),
                appliedAt);
    }
}

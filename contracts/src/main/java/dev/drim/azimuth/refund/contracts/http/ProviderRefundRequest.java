package dev.drim.azimuth.refund.contracts.http;

import java.math.BigDecimal;
import java.util.UUID;

public record ProviderRefundRequest(
        UUID refundId,
        UUID paymentId,
        BigDecimal amount,
        String currency
) {
}

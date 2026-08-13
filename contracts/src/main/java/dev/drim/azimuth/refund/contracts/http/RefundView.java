package dev.drim.azimuth.refund.contracts.http;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RefundView(
        UUID refundId,
        UUID paymentId,
        BigDecimal amount,
        String currency,
        RefundStatus status,
        UUID providerOperationId,
        Instant approvedAt,
        Instant completedAt
) {
}

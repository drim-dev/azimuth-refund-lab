package dev.drim.azimuth.refund.contracts.http;

import java.util.List;
import java.util.UUID;

public record ProviderRefundSummary(
        UUID refundId,
        List<ProviderRefundAttempt> attempts,
        List<ProviderRefundResponse> operations,
        int applicationCount
) {
}

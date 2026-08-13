package dev.drim.azimuth.refund.contracts.http;

import java.util.List;
import java.util.UUID;

public record TripFinancialView(
        UUID tripId,
        UUID riderId,
        UUID paymentId,
        Money capturedAmount,
        Money originalFare,
        Money waitTimeFee,
        Money approvedRefundTotal,
        Money finalCost,
        List<RefundView> refunds
) {
}

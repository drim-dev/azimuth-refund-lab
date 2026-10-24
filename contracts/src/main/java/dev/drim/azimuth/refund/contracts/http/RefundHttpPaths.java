package dev.drim.azimuth.refund.contracts.http;

public final class RefundHttpPaths {
    public static final String APPROVE_REFUND = "/api/v1/payments/{paymentId}/refunds";
    public static final String REFUND_BY_ID = "/api/v1/refunds/{refundId}";
    public static final String TRIP_BY_ID = "/api/v1/trips/{tripId}";
    public static final String PROVIDER_REFUNDS = "/provider/v1/refunds";
    public static final String PROVIDER_REFUND_BY_KEY = "/provider/v1/refunds/{idempotencyKey}";
    public static final String PROVIDER_REFUND_SUMMARY =
            "/provider/v1/business-refunds/{refundId}";

    private RefundHttpPaths() {
    }
}

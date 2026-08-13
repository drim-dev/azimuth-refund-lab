package dev.drim.azimuth.refund.contracts.http;

public final class RefundHttpPaths {
    public static final String APPROVE_REFUND = "/api/v1/payments/{paymentId}/refunds";
    public static final String REFUND_BY_ID = "/api/v1/refunds/{refundId}";
    public static final String TRIP_BY_ID = "/api/v1/trips/{tripId}";
    public static final String PROVIDER_REFUND_BY_ID = "/provider/v1/refunds/{refundId}";

    private RefundHttpPaths() {
    }
}

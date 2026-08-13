package dev.drim.azimuth.refund.contracts.event;

public final class RefundMessaging {
    public static final String EXCHANGE = "refund.events";
    public static final String REQUESTED_ROUTING_KEY = RefundEventNames.REFUND_REQUESTED_V1;
    public static final String RESULT_ROUTING_KEY = RefundEventNames.REFUND_RESULT_V1;
    public static final String WORKER_QUEUE = "refund.worker.refund-requested.v1";
    public static final String API_RESULT_QUEUE = "refund.api.refund-result.v1";

    private RefundMessaging() {
    }
}

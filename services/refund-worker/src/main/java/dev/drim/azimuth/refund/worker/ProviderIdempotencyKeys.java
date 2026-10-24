package dev.drim.azimuth.refund.worker;

import dev.drim.azimuth.Azimuth.ImplementsMechanism;
import dev.drim.azimuth.Azimuth.Realizes;
import java.util.UUID;

final class ProviderIdempotencyKeys {
    private final ProviderIdempotencyKeyMode mode;

    ProviderIdempotencyKeys(ProviderIdempotencyKeyMode mode) {
        this.mode = mode;
    }

    @Realizes(spec = "refunds/execution", scenario = "repeated-processing-does-not-repeat-refund")
    @ImplementsMechanism(
            spec = "refunds/execution",
            mechanism = "stable-provider-idempotency-key")
    String forRefund(UUID refundId) {
        if (mode == ProviderIdempotencyKeyMode.STABLE_REFUND_ID) {
            return refundId.toString();
        }
        return UUID.randomUUID().toString();
    }
}

enum ProviderIdempotencyKeyMode {
    STABLE_REFUND_ID,
    PER_DELIVERY
}

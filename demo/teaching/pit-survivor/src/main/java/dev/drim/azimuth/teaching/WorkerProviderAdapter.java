package dev.drim.azimuth.teaching;

import java.util.UUID;

final class WorkerProviderAdapter {
    private final boolean newKeyPerDelivery;

    WorkerProviderAdapter(boolean newKeyPerDelivery) {
        this.newKeyPerDelivery = newKeyPerDelivery;
    }

    String keyFor(String refundId) {
        if (!newKeyPerDelivery) {
            return refundId;
        }
        return UUID.randomUUID().toString();
    }
}

package dev.drim.azimuth.refund.worker;

import java.util.UUID;

interface DeliveryBoundary {
    void afterProviderResponse(UUID refundId);
}

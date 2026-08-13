package dev.drim.azimuth.refund.worker;

import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!demo")
class ContinuousDeliveryBoundary implements DeliveryBoundary {
    @Override
    public void afterProviderResponse(UUID refundId) {
    }
}

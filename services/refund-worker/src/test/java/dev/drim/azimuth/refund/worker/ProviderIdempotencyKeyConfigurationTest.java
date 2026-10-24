package dev.drim.azimuth.refund.worker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

final class ProviderIdempotencyKeyConfigurationTest {
    private final ProviderIdempotencyKeyConfiguration configuration =
            new ProviderIdempotencyKeyConfiguration();

    @Test
    void unsafeModeIsRejectedOutsideTheDemoProfile() {
        MockEnvironment environment = new MockEnvironment();

        assertThatThrownBy(() -> configuration.providerIdempotencyKeys(
                new ProviderIdempotencyKeyConfiguration.DemoBehaviorProperties(true),
                environment))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("requires the demo profile");
    }

    @Test
    void demoProfileCanSelectPerDeliveryKeys() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("demo");
        ProviderIdempotencyKeys keys = configuration.providerIdempotencyKeys(
                new ProviderIdempotencyKeyConfiguration.DemoBehaviorProperties(true),
                environment);
        UUIDPair pair = UUIDPair.from(keys);

        assertThat(pair.first()).isNotEqualTo(pair.second());
    }

    private record UUIDPair(String first, String second) {
        static UUIDPair from(ProviderIdempotencyKeys keys) {
            java.util.UUID refundId = java.util.UUID.randomUUID();
            return new UUIDPair(keys.forRefund(refundId), keys.forRefund(refundId));
        }
    }
}

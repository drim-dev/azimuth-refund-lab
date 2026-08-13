package dev.drim.azimuth.refund.provider;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class ProviderApplicationModeConfigurationTest {
    @Test
    void duplicateApplicationModeIsRejectedWithoutTheDemoProfile() {
        ProviderApplicationModeConfiguration configuration =
                new ProviderApplicationModeConfiguration();
        ProviderApplicationModeConfiguration.DemoBehaviorProperties unsafeBehavior =
                new ProviderApplicationModeConfiguration.DemoBehaviorProperties(true);

        assertThatThrownBy(() -> configuration.providerRefundApplicationMode(
                unsafeBehavior, new MockEnvironment()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("requires the demo profile");
    }
}

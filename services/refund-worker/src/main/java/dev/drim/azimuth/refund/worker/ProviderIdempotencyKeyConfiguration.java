package dev.drim.azimuth.refund.worker;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ProviderIdempotencyKeyConfiguration.DemoBehaviorProperties.class)
class ProviderIdempotencyKeyConfiguration {
    private static final Profiles DEMO_PROFILE = Profiles.of("demo");

    @Bean
    ProviderIdempotencyKeys providerIdempotencyKeys(
            DemoBehaviorProperties demoBehavior,
            Environment environment
    ) {
        if (demoBehavior.newKeyPerDelivery() && !environment.acceptsProfiles(DEMO_PROFILE)) {
            throw new IllegalStateException(
                    "refund.provider.demo.new-key-per-delivery requires the demo profile");
        }
        ProviderIdempotencyKeyMode mode = demoBehavior.newKeyPerDelivery()
                ? ProviderIdempotencyKeyMode.PER_DELIVERY
                : ProviderIdempotencyKeyMode.STABLE_REFUND_ID;
        return new ProviderIdempotencyKeys(mode);
    }

    @ConfigurationProperties("refund.provider.demo")
    record DemoBehaviorProperties(boolean newKeyPerDelivery) {
    }
}

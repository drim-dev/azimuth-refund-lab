package dev.drim.azimuth.refund.provider;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ProviderApplicationModeConfiguration.DemoBehaviorProperties.class)
class ProviderApplicationModeConfiguration {
    private static final Profiles DEMO_PROFILE = Profiles.of("demo");

    @Bean
    ProviderRefundApplicationMode providerRefundApplicationMode(
            DemoBehaviorProperties demoBehavior,
            Environment environment
    ) {
        if (demoBehavior.reapplyIdenticalRequests()
                && !environment.acceptsProfiles(DEMO_PROFILE)) {
            throw new IllegalStateException(
                    "provider.demo.reapply-identical-requests requires the demo profile");
        }
        return demoBehavior.reapplyIdenticalRequests()
                ? ProviderRefundApplicationMode.REAPPLY_IDENTICAL_REQUESTS
                : ProviderRefundApplicationMode.IDEMPOTENT;
    }

    @ConfigurationProperties("provider.demo")
    record DemoBehaviorProperties(boolean reapplyIdenticalRequests) {
    }
}

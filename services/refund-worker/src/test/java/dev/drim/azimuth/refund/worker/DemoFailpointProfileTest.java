package dev.drim.azimuth.refund.worker;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

class DemoFailpointProfileTest {
    @Test
    void controllerIsAbsentWithoutDemoProfile() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.register(DemoDeliveryFailpoint.class, DemoFailpointController.class);
            context.refresh();

            assertThat(context.getBeansOfType(DemoDeliveryFailpoint.class)).isEmpty();
            assertThat(context.getBeansOfType(DemoFailpointController.class)).isEmpty();
        }
    }
}

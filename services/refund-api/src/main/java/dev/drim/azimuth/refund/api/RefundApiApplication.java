package dev.drim.azimuth.refund.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class RefundApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(RefundApiApplication.class, args);
    }
}

package dev.drim.azimuth.refund.api.config;

import dev.drim.azimuth.refund.contracts.event.RefundMessaging;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfiguration {
    @Bean
    DirectExchange refundEventsExchange() {
        return new DirectExchange(RefundMessaging.EXCHANGE, true, false);
    }

    @Bean
    Queue workerQueue() {
        return new Queue(RefundMessaging.WORKER_QUEUE, true);
    }

    @Bean
    Queue apiResultQueue() {
        return new Queue(RefundMessaging.API_RESULT_QUEUE, true);
    }

    @Bean
    Binding requestedEventBinding(DirectExchange refundEventsExchange, Queue workerQueue) {
        return BindingBuilder.bind(workerQueue)
                .to(refundEventsExchange)
                .with(RefundMessaging.REQUESTED_ROUTING_KEY);
    }

    @Bean
    Binding resultEventBinding(DirectExchange refundEventsExchange, Queue apiResultQueue) {
        return BindingBuilder.bind(apiResultQueue)
                .to(refundEventsExchange)
                .with(RefundMessaging.RESULT_ROUTING_KEY);
    }
}

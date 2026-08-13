package dev.drim.azimuth.refund.worker;

import dev.drim.azimuth.refund.contracts.event.RefundMessaging;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class RefundMessagingConfiguration {
    @Bean
    DirectExchange refundEventsExchange() {
        return new DirectExchange(RefundMessaging.EXCHANGE, true, false);
    }

    @Bean
    Queue refundRequestedQueue() {
        return QueueBuilder.durable(RefundMessaging.WORKER_QUEUE).build();
    }

    @Bean
    Queue refundResultQueue() {
        return QueueBuilder.durable(RefundMessaging.API_RESULT_QUEUE).build();
    }

    @Bean
    Binding refundRequestedBinding(DirectExchange refundEventsExchange, Queue refundRequestedQueue) {
        return BindingBuilder.bind(refundRequestedQueue)
                .to(refundEventsExchange)
                .with(RefundMessaging.REQUESTED_ROUTING_KEY);
    }

    @Bean
    Binding refundResultBinding(DirectExchange refundEventsExchange, Queue refundResultQueue) {
        return BindingBuilder.bind(refundResultQueue)
                .to(refundEventsExchange)
                .with(RefundMessaging.RESULT_ROUTING_KEY);
    }
}

package dev.drim.azimuth.teaching;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

final class RefundRedeliveryComposedTest {
    @Test
    @Tag("weak-oracle")
    void refundRedeliveryReturnsSucceededStatus() {
        var adapter = new WorkerProviderAdapter(false);
        var provider = new PaymentProvider();

        var first = provider.refund(adapter.keyFor("refund-01"));
        var repeated = provider.refund(adapter.keyFor("refund-01"));

        assertEquals(first, repeated);
    }

    @Test
    @Tag("direct-oracle")
    void refundRedeliveryObservesTwoAttemptsOneKeyAndOneEffect() {
        var adapter = new WorkerProviderAdapter(false);
        var provider = new PaymentProvider();

        provider.refund(adapter.keyFor("refund-01"));
        provider.refund(adapter.keyFor("refund-01"));

        assertEquals(List.of("refund-01", "refund-01"), provider.attempts());
        assertEquals(1, provider.operationCount());
    }
}

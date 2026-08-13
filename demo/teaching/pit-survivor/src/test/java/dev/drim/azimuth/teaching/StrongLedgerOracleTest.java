package dev.drim.azimuth.teaching;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class StrongLedgerOracleTest {
    @Test
    void duplicateDeliveryProducesOneFinancialEffect() {
        var effect = new RefundEffect();

        effect.apply("refund-01");
        effect.apply("refund-01");

        assertEquals(1, effect.applicationCount());
    }
}

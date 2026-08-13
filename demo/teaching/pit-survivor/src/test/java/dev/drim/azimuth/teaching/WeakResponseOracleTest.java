package dev.drim.azimuth.teaching;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class WeakResponseOracleTest {
    @Test
    void duplicateResponsesLookConsistent() {
        var effect = new RefundEffect();

        var first = effect.apply("refund-01");
        var repeated = effect.apply("refund-01");

        assertEquals(first, repeated);
    }
}

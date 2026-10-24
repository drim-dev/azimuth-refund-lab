package dev.drim.azimuth.refund.worker;

import static org.assertj.core.api.Assertions.assertThat;

import dev.drim.azimuth.Azimuth.Covers;
import dev.drim.azimuth.Azimuth.CoversMechanism;
import dev.drim.azimuth.Azimuth.Oracle;
import dev.drim.azimuth.Azimuth.Quantification;
import dev.drim.azimuth.Azimuth.Scope;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

final class ProviderIdempotencyKeysTest {
    @Test
    @Tag("mutation")
    @Covers(
            spec = "refunds/execution",
            scenario = "repeated-processing-does-not-repeat-refund",
            scope = Scope.unit,
            quantification = Quantification.universal,
            oracle = Oracle.relational)
    @CoversMechanism(
            spec = "refunds/execution",
            mechanism = "stable-provider-idempotency-key",
            scope = Scope.unit,
            quantification = Quantification.universal,
            oracle = Oracle.relational)
    void everyGeneratedRefundKeepsItsBusinessIdentityAcrossRepeatedProviderCalls() {
        ProviderIdempotencyKeys keys = new ProviderIdempotencyKeys(
                ProviderIdempotencyKeyMode.STABLE_REFUND_ID);

        IntStream.range(0, 1_000)
                .mapToObj(index -> UUID.nameUUIDFromBytes(
                        ("refund-property-" + index).getBytes(StandardCharsets.UTF_8)))
                .forEach(refundId -> assertThat(IntStream.range(0, 20)
                        .mapToObj(ignored -> keys.forRefund(refundId)))
                        .containsOnly(refundId.toString()));
    }

    @Test
    void demoOnlyPerDeliveryModeCreatesIndependentProviderOperations() {
        ProviderIdempotencyKeys keys = new ProviderIdempotencyKeys(
                ProviderIdempotencyKeyMode.PER_DELIVERY);
        UUID refundId = UUID.randomUUID();

        assertThat(keys.forRefund(refundId)).isNotEqualTo(keys.forRefund(refundId));
    }
}

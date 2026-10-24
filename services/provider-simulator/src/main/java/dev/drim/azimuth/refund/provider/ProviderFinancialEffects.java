package dev.drim.azimuth.refund.provider;

import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class ProviderFinancialEffects {
    private static final String INSERT = """
            INSERT INTO provider_financial_effects (
                effect_id,
                idempotency_key,
                refund_id,
                payment_id,
                amount,
                currency,
                applied_at
            ) VALUES (
                :effectId,
                :idempotencyKey,
                :refundId,
                :paymentId,
                :amount,
                :currency,
                :appliedAt
            )
            """;

    private static final String COUNT_BY_REFUND_ID = """
            SELECT COUNT(*)
            FROM provider_financial_effects
            WHERE refund_id = :refundId
            """;

    private final NamedParameterJdbcTemplate jdbc;

    ProviderFinancialEffects(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    void record(ProviderFinancialEffect effect) {
        jdbc.update(INSERT, Map.of(
                "effectId", effect.effectId(),
                "idempotencyKey", effect.idempotencyKey(),
                "refundId", effect.refundId(),
                "paymentId", effect.paymentId(),
                "amount", effect.amount(),
                "currency", effect.currency(),
                "appliedAt", Timestamp.from(effect.appliedAt())));
    }

    int count(UUID refundId) {
        Integer count = jdbc.queryForObject(
                COUNT_BY_REFUND_ID,
                Map.of("refundId", refundId),
                Integer.class);
        return count == null ? 0 : count;
    }
}

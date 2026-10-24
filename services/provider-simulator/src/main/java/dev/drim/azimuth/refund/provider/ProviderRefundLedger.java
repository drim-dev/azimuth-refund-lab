package dev.drim.azimuth.refund.provider;

import dev.drim.azimuth.Azimuth.Realizes;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class ProviderRefundLedger {
    private static final String INSERT_IF_ABSENT = """
            INSERT INTO provider_refund_ledger (
                idempotency_key,
                refund_id,
                payment_id,
                amount,
                currency,
                provider_operation_id,
                outcome,
                applied_at
            ) VALUES (
                :idempotencyKey,
                :refundId,
                :paymentId,
                :amount,
                :currency,
                :providerOperationId,
                :outcome,
                :appliedAt
            )
            ON CONFLICT (idempotency_key) DO NOTHING
            """;

    private static final String FIND_BY_IDEMPOTENCY_KEY = """
            SELECT
                idempotency_key,
                refund_id,
                payment_id,
                amount,
                currency,
                provider_operation_id,
                outcome,
                applied_at
            FROM provider_refund_ledger
            WHERE idempotency_key = :idempotencyKey
            """;

    private static final String FIND_BY_REFUND_ID = """
            SELECT
                idempotency_key,
                refund_id,
                payment_id,
                amount,
                currency,
                provider_operation_id,
                outcome,
                applied_at
            FROM provider_refund_ledger
            WHERE refund_id = :refundId
            ORDER BY applied_at, idempotency_key
            """;

    private final NamedParameterJdbcTemplate jdbc;

    ProviderRefundLedger(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Realizes(spec = "refunds/execution", scenario = "repeated-processing-does-not-repeat-refund")
    int insertIfAbsent(ProviderLedgerEntry entry) {
        return jdbc.update(INSERT_IF_ABSENT, Map.of(
                "idempotencyKey", entry.idempotencyKey(),
                "refundId", entry.refundId(),
                "paymentId", entry.paymentId(),
                "amount", entry.amount(),
                "currency", entry.currency(),
                "providerOperationId", entry.providerOperationId(),
                "outcome", entry.outcome(),
                "appliedAt", Timestamp.from(entry.appliedAt())));
    }

    ProviderLedgerEntry findRequired(String idempotencyKey) {
        return find(idempotencyKey).orElseThrow();
    }

    Optional<ProviderLedgerEntry> find(String idempotencyKey) {
        return jdbc.query(
                FIND_BY_IDEMPOTENCY_KEY,
                Map.of("idempotencyKey", idempotencyKey),
                ProviderRefundLedger::mapEntry)
                .stream()
                .findFirst();
    }

    java.util.List<ProviderLedgerEntry> findByRefundId(UUID refundId) {
        return jdbc.query(
                FIND_BY_REFUND_ID,
                Map.of("refundId", refundId),
                ProviderRefundLedger::mapEntry);
    }

    private static ProviderLedgerEntry mapEntry(ResultSet resultSet, int rowNumber)
            throws SQLException {
        return new ProviderLedgerEntry(
                resultSet.getString("idempotency_key"),
                resultSet.getObject("refund_id", UUID.class),
                resultSet.getObject("payment_id", UUID.class),
                resultSet.getBigDecimal("amount"),
                resultSet.getString("currency"),
                resultSet.getObject("provider_operation_id", UUID.class),
                resultSet.getString("outcome"),
                resultSet.getObject("applied_at", OffsetDateTime.class).toInstant());
    }
}

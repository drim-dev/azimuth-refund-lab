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
                refund_id,
                payment_id,
                amount,
                currency,
                provider_operation_id,
                outcome,
                applied_at
            ) VALUES (
                :refundId,
                :paymentId,
                :amount,
                :currency,
                :providerOperationId,
                :outcome,
                :appliedAt
            )
            ON CONFLICT (refund_id) DO NOTHING
            """;

    private static final String FIND_BY_REFUND_ID = """
            SELECT
                refund_id,
                payment_id,
                amount,
                currency,
                provider_operation_id,
                outcome,
                applied_at
            FROM provider_refund_ledger
            WHERE refund_id = :refundId
            """;

    private final NamedParameterJdbcTemplate jdbc;

    ProviderRefundLedger(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Realizes(spec = "refunds/provider-effects", scenario = "redelivery-after-provider-success")
    int insertIfAbsent(ProviderLedgerEntry entry) {
        return jdbc.update(INSERT_IF_ABSENT, Map.of(
                "refundId", entry.refundId(),
                "paymentId", entry.paymentId(),
                "amount", entry.amount(),
                "currency", entry.currency(),
                "providerOperationId", entry.providerOperationId(),
                "outcome", entry.outcome(),
                "appliedAt", Timestamp.from(entry.appliedAt())));
    }

    ProviderLedgerEntry findRequired(UUID refundId) {
        return find(refundId).orElseThrow();
    }

    Optional<ProviderLedgerEntry> find(UUID refundId) {
        return jdbc.query(
                FIND_BY_REFUND_ID,
                Map.of("refundId", refundId),
                ProviderRefundLedger::mapEntry)
                .stream()
                .findFirst();
    }

    private static ProviderLedgerEntry mapEntry(ResultSet resultSet, int rowNumber)
            throws SQLException {
        return new ProviderLedgerEntry(
                resultSet.getObject("refund_id", UUID.class),
                resultSet.getObject("payment_id", UUID.class),
                resultSet.getBigDecimal("amount"),
                resultSet.getString("currency"),
                resultSet.getObject("provider_operation_id", UUID.class),
                resultSet.getString("outcome"),
                resultSet.getObject("applied_at", OffsetDateTime.class).toInstant());
    }
}

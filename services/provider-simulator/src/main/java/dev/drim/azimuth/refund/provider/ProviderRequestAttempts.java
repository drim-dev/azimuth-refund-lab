package dev.drim.azimuth.refund.provider;

import dev.drim.azimuth.refund.contracts.http.ProviderRefundAttempt;
import dev.drim.azimuth.refund.contracts.http.ProviderRefundRequest;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class ProviderRequestAttempts {
    private final NamedParameterJdbcTemplate jdbc;

    ProviderRequestAttempts(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    void record(String idempotencyKey, ProviderRefundRequest request, Instant attemptedAt) {
        jdbc.update("""
                INSERT INTO provider_request_attempts (
                    attempt_id, idempotency_key, refund_id, payment_id, amount, currency, attempted_at
                ) VALUES (
                    :attemptId, :idempotencyKey, :refundId, :paymentId, :amount, :currency, :attemptedAt
                )
                """, Map.of(
                "attemptId", UUID.randomUUID(),
                "idempotencyKey", idempotencyKey,
                "refundId", request.refundId(),
                "paymentId", request.paymentId(),
                "amount", request.amount(),
                "currency", request.currency(),
                "attemptedAt", Timestamp.from(attemptedAt)));
    }

    List<ProviderRefundAttempt> findByRefundId(UUID refundId) {
        return jdbc.query("""
                SELECT idempotency_key, refund_id, payment_id, amount, currency, attempted_at
                FROM provider_request_attempts
                WHERE refund_id = :refundId
                ORDER BY attempted_at, attempt_id
                """, Map.of("refundId", refundId), ProviderRequestAttempts::mapAttempt);
    }

    private static ProviderRefundAttempt mapAttempt(ResultSet resultSet, int rowNumber)
            throws SQLException {
        return new ProviderRefundAttempt(
                resultSet.getString("idempotency_key"),
                resultSet.getObject("refund_id", UUID.class),
                resultSet.getObject("payment_id", UUID.class),
                resultSet.getBigDecimal("amount"),
                resultSet.getString("currency"),
                resultSet.getObject("attempted_at", OffsetDateTime.class).toInstant());
    }
}

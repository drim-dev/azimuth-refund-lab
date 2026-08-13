package dev.drim.azimuth.refund.api.refund;

import dev.drim.azimuth.refund.contracts.http.Money;
import dev.drim.azimuth.refund.contracts.http.RefundStatus;
import dev.drim.azimuth.refund.contracts.http.RefundView;
import dev.drim.azimuth.refund.contracts.http.TripFinancialView;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RefundRepository {
    private final JdbcTemplate jdbc;

    public RefundRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<Payment> findPayment(UUID paymentId) {
        return jdbc.query("""
                SELECT payment_id, currency, refundable_remaining
                FROM payments
                WHERE payment_id = ?
                """, this::mapPayment, paymentId).stream().findFirst();
    }

    public boolean reserveCapacity(UUID paymentId, BigDecimal amount, String currency) {
        return jdbc.update("""
                UPDATE payments
                SET refundable_remaining = refundable_remaining - ?
                WHERE payment_id = ? AND currency = ? AND refundable_remaining >= ?
                """, amount, paymentId, currency, amount) == 1;
    }

    public void insertRefund(
            UUID refundId,
            UUID paymentId,
            BigDecimal amount,
            String currency,
            String approvedBy,
            Instant approvedAt
    ) {
        jdbc.update("""
                INSERT INTO refunds (
                    refund_id, payment_id, amount, currency, status, approved_by, approved_at
                ) VALUES (?, ?, ?, ?, 'pending', ?, ?)
                """, refundId, paymentId, amount, currency, approvedBy, Timestamp.from(approvedAt));
    }

    public void insertOutbox(UUID eventId, UUID refundId, String eventType, String payload, Instant createdAt) {
        jdbc.update("""
                INSERT INTO refund_request_outbox (event_id, refund_id, event_type, payload, created_at)
                VALUES (?, ?, ?, CAST(? AS jsonb), ?)
                """, eventId, refundId, eventType, payload, Timestamp.from(createdAt));
    }

    public Optional<RefundView> findRefund(UUID refundId) {
        return jdbc.query("""
                SELECT refund_id, payment_id, amount, currency, status, provider_operation_id,
                       approved_at, completed_at
                FROM refunds
                WHERE refund_id = ?
                """, this::mapRefund, refundId).stream().findFirst();
    }

    public Optional<TripFinancialView> findTrip(UUID tripId) {
        List<TripRow> trips = jdbc.query("""
                SELECT t.trip_id, t.rider_id, p.payment_id, p.captured_amount, p.original_fare,
                       p.wait_time_fee, p.refundable_remaining, p.currency
                FROM trips t
                JOIN payments p ON p.trip_id = t.trip_id
                WHERE t.trip_id = ?
                """, this::mapTrip, tripId);
        if (trips.isEmpty()) {
            return Optional.empty();
        }

        TripRow trip = trips.getFirst();
        List<RefundView> refunds = jdbc.query("""
                SELECT refund_id, payment_id, amount, currency, status, provider_operation_id,
                       approved_at, completed_at
                FROM refunds
                WHERE payment_id = ?
                ORDER BY approved_at, refund_id
                """, this::mapRefund, trip.paymentId());
        BigDecimal approvedTotal = trip.capturedAmount().subtract(trip.refundableRemaining());
        return Optional.of(new TripFinancialView(
                trip.tripId(),
                trip.riderId(),
                trip.paymentId(),
                new Money(trip.capturedAmount(), trip.currency()),
                new Money(trip.originalFare(), trip.currency()),
                new Money(trip.waitTimeFee(), trip.currency()),
                new Money(approvedTotal, trip.currency()),
                new Money(trip.capturedAmount().subtract(approvedTotal), trip.currency()),
                refunds
        ));
    }

    public boolean recordResultReceipt(UUID eventId, UUID refundId, Instant receivedAt) {
        return jdbc.update("""
                INSERT INTO refund_result_inbox (event_id, refund_id, received_at)
                VALUES (?, ?, ?)
                ON CONFLICT (event_id) DO NOTHING
                """, eventId, refundId, Timestamp.from(receivedAt)) == 1;
    }

    public boolean completeRefund(UUID refundId, UUID providerOperationId, Instant appliedAt) {
        return jdbc.update("""
                UPDATE refunds
                SET status = 'succeeded', provider_operation_id = ?, completed_at = ?
                WHERE refund_id = ?
                  AND (status = 'pending' OR provider_operation_id = ?)
                """, providerOperationId, Timestamp.from(appliedAt), refundId, providerOperationId) == 1;
    }

    private Payment mapPayment(ResultSet row, int rowNumber) throws SQLException {
        return new Payment(
                row.getObject("payment_id", UUID.class),
                row.getString("currency").strip(),
                row.getBigDecimal("refundable_remaining")
        );
    }

    private RefundView mapRefund(ResultSet row, int rowNumber) throws SQLException {
        Timestamp completedAt = row.getTimestamp("completed_at");
        return new RefundView(
                row.getObject("refund_id", UUID.class),
                row.getObject("payment_id", UUID.class),
                row.getBigDecimal("amount"),
                row.getString("currency").strip(),
                RefundStatus.fromWireName(row.getString("status")),
                row.getObject("provider_operation_id", UUID.class),
                row.getTimestamp("approved_at").toInstant(),
                completedAt == null ? null : completedAt.toInstant()
        );
    }

    private TripRow mapTrip(ResultSet row, int rowNumber) throws SQLException {
        return new TripRow(
                row.getObject("trip_id", UUID.class),
                row.getObject("rider_id", UUID.class),
                row.getObject("payment_id", UUID.class),
                row.getBigDecimal("captured_amount"),
                row.getBigDecimal("original_fare"),
                row.getBigDecimal("wait_time_fee"),
                row.getBigDecimal("refundable_remaining"),
                row.getString("currency").strip()
        );
    }

    public record Payment(UUID paymentId, String currency, BigDecimal refundableRemaining) {
    }

    private record TripRow(
            UUID tripId,
            UUID riderId,
            UUID paymentId,
            BigDecimal capturedAmount,
            BigDecimal originalFare,
            BigDecimal waitTimeFee,
            BigDecimal refundableRemaining,
            String currency
    ) {
    }
}

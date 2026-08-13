package dev.drim.azimuth.refund.api.refund;

import dev.drim.azimuth.refund.api.http.ApiExceptionHandler.ApiProblem;
import dev.drim.azimuth.refund.contracts.event.RefundEventNames;
import dev.drim.azimuth.refund.contracts.event.RefundRequestedV1;
import dev.drim.azimuth.refund.contracts.http.ApproveRefundRequest;
import dev.drim.azimuth.refund.contracts.http.RefundView;
import dev.drim.azimuth.refund.contracts.http.TripFinancialView;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
public class RefundService {
    private static final String FIXTURE_SUPPORT_IDENTITY = "support-fixture";

    private final RefundRepository refunds;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Autowired
    public RefundService(RefundRepository refunds, ObjectMapper objectMapper) {
        this(refunds, objectMapper, Clock.systemUTC());
    }

    RefundService(RefundRepository refunds, ObjectMapper objectMapper, Clock clock) {
        this.refunds = refunds;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Transactional
    public RefundView approve(UUID paymentId, ApproveRefundRequest request) {
        validate(request);
        String currency = request.currency().toUpperCase(Locale.ROOT);
        RefundRepository.Payment payment = refunds.findPayment(paymentId)
                .orElseThrow(() -> problem(HttpStatus.NOT_FOUND, "Payment not found", "payment-not-found",
                        "No captured payment exists for " + paymentId));
        if (!payment.currency().equals(currency)) {
            throw problem(HttpStatus.CONFLICT, "Currency mismatch", "currency-mismatch",
                    "Refund currency must match the captured payment currency");
        }
        if (!refunds.reserveCapacity(paymentId, request.amount(), currency)) {
            throw problem(HttpStatus.CONFLICT, "Refund capacity exceeded", "capacity-exceeded",
                    "The requested refund exceeds the remaining captured amount");
        }

        UUID refundId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        Instant approvedAt = clock.instant().truncatedTo(ChronoUnit.MILLIS);
        RefundRequestedV1 event = new RefundRequestedV1(
                eventId,
                RefundEventNames.REFUND_REQUESTED_V1,
                RefundEventNames.VERSION_1,
                approvedAt,
                refundId,
                paymentId,
                request.amount(),
                currency,
                FIXTURE_SUPPORT_IDENTITY
        );
        refunds.insertRefund(refundId, paymentId, request.amount(), currency, FIXTURE_SUPPORT_IDENTITY, approvedAt);
        refunds.insertOutbox(eventId, refundId, event.eventType(), serialize(event), approvedAt);
        return refunds.findRefund(refundId).orElseThrow();
    }

    @Transactional(readOnly = true)
    public RefundView getRefund(UUID refundId) {
        return refunds.findRefund(refundId)
                .orElseThrow(() -> problem(HttpStatus.NOT_FOUND, "Refund not found", "refund-not-found",
                        "No refund exists for " + refundId));
    }

    @Transactional(readOnly = true)
    public TripFinancialView getTrip(UUID tripId) {
        return refunds.findTrip(tripId)
                .orElseThrow(() -> problem(HttpStatus.NOT_FOUND, "Trip not found", "trip-not-found",
                        "No trip exists for " + tripId));
    }

    private void validate(ApproveRefundRequest request) {
        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0
                || request.amount().scale() > 2) {
            throw problem(HttpStatus.BAD_REQUEST, "Invalid refund", "invalid-amount",
                    "Refund amount must be positive and have at most two decimal places");
        }
        if (request.currency() == null || !request.currency().matches("[A-Z]{3}")) {
            throw problem(HttpStatus.BAD_REQUEST, "Invalid refund", "invalid-currency",
                    "Currency must be a three-letter uppercase code");
        }
    }

    private String serialize(RefundRequestedV1 event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Refund request event could not be serialized", exception);
        }
    }

    private ApiProblem problem(HttpStatus status, String title, String code, String detail) {
        return new ApiProblem(status, title, code, detail);
    }
}

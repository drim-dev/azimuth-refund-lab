package dev.drim.azimuth.refund.provider;

import dev.drim.azimuth.Azimuth.ImplementsMechanism;
import dev.drim.azimuth.Azimuth.Realizes;
import dev.drim.azimuth.refund.contracts.event.RefundOutcome;
import dev.drim.azimuth.refund.contracts.http.ProviderRefundRequest;
import dev.drim.azimuth.refund.contracts.http.ProviderRefundResponse;
import dev.drim.azimuth.refund.contracts.http.ProviderRefundSummary;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class ProviderRefundService {
    private static final String SUCCEEDED = RefundOutcome.SUCCEEDED.wireName();

    private final ProviderRefundLedger providerRefundLedger;
    private final ProviderFinancialEffects providerFinancialEffects;
    private final ProviderRequestAttempts providerRequestAttempts;
    private final Clock clock = Clock.systemUTC();

    ProviderRefundService(
            ProviderRefundLedger providerRefundLedger,
            ProviderFinancialEffects providerFinancialEffects,
            ProviderRequestAttempts providerRequestAttempts
    ) {
        this.providerRefundLedger = providerRefundLedger;
        this.providerFinancialEffects = providerFinancialEffects;
        this.providerRequestAttempts = providerRequestAttempts;
    }

    @Transactional
    @Realizes(spec = "refunds/execution", scenario = "repeated-processing-does-not-repeat-refund")
    @ImplementsMechanism(spec = "refunds/execution", mechanism = "provider-idempotency-contract")
    public ProviderRefundResponse apply(String idempotencyKey, ProviderRefundRequest request) {
        validate(idempotencyKey, request);
        providerRequestAttempts.record(idempotencyKey, request, Instant.now(clock));

        ProviderLedgerEntry candidate = new ProviderLedgerEntry(
                idempotencyKey,
                request.refundId(),
                request.paymentId(),
                request.amount(),
                request.currency(),
                UUID.randomUUID(),
                SUCCEEDED,
                Instant.now(clock));

        boolean createdIdempotencyRecord = providerRefundLedger.insertIfAbsent(candidate) == 1;
        ProviderLedgerEntry stored = providerRefundLedger.findRequired(idempotencyKey);
        if (!stored.matches(request)) {
            throw new ProviderRefundConflictException(idempotencyKey);
        }
        if (createdIdempotencyRecord) {
            providerFinancialEffects.record(ProviderFinancialEffect.from(stored, Instant.now(clock)));
        }

        return stored.toResponse();
    }

    public ProviderRefundResponse get(String idempotencyKey) {
        return providerRefundLedger.find(idempotencyKey)
                .map(ProviderLedgerEntry::toResponse)
                .orElseThrow(() -> new ProviderRefundNotFoundException(idempotencyKey));
    }

    public ProviderRefundSummary summarize(UUID refundId) {
        return new ProviderRefundSummary(
                refundId,
                providerRequestAttempts.findByRefundId(refundId),
                providerRefundLedger.findByRefundId(refundId).stream()
                        .map(ProviderLedgerEntry::toResponse)
                        .toList(),
                providerFinancialEffects.count(refundId));
    }

    private static void validate(String idempotencyKey, ProviderRefundRequest request) {
        if (idempotencyKey == null || idempotencyKey.isBlank() || idempotencyKey.length() > 255) {
            throw new InvalidProviderRefundException(
                    "Idempotency-Key must contain between 1 and 255 characters");
        }
        if (request == null || request.refundId() == null) {
            throw new InvalidProviderRefundException("refundId is required");
        }
        if (request.paymentId() == null) {
            throw new InvalidProviderRefundException("paymentId is required");
        }
        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidProviderRefundException("amount must be greater than zero");
        }
        if (request.currency() == null || !request.currency().matches("[A-Z]{3}")) {
            throw new InvalidProviderRefundException("currency must be a three-letter uppercase code");
        }
    }
}

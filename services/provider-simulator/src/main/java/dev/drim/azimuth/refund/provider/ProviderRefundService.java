package dev.drim.azimuth.refund.provider;

import dev.drim.azimuth.Azimuth.ImplementsMechanism;
import dev.drim.azimuth.Azimuth.Realizes;
import dev.drim.azimuth.refund.contracts.event.RefundOutcome;
import dev.drim.azimuth.refund.contracts.http.ProviderRefundRequest;
import dev.drim.azimuth.refund.contracts.http.ProviderRefundResponse;
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
    private final ProviderRefundApplicationMode applicationMode;
    private final Clock clock = Clock.systemUTC();

    ProviderRefundService(
            ProviderRefundLedger providerRefundLedger,
            ProviderFinancialEffects providerFinancialEffects,
            ProviderRefundApplicationMode applicationMode
    ) {
        this.providerRefundLedger = providerRefundLedger;
        this.providerFinancialEffects = providerFinancialEffects;
        this.applicationMode = applicationMode;
    }

    @Transactional
    @Realizes(spec = "refunds/provider-effects", scenario = "redelivery-after-provider-success")
    @ImplementsMechanism(spec = "refunds/provider-effects", mechanism = "provider-refund-ledger")
    public ProviderRefundResponse apply(UUID refundId, ProviderRefundRequest request) {
        validate(refundId, request);

        ProviderLedgerEntry candidate = new ProviderLedgerEntry(
                refundId,
                request.paymentId(),
                request.amount(),
                request.currency(),
                UUID.randomUUID(),
                SUCCEEDED,
                Instant.now(clock));

        boolean createdIdempotencyRecord = providerRefundLedger.insertIfAbsent(candidate) == 1;
        ProviderLedgerEntry stored = providerRefundLedger.findRequired(refundId);
        if (!stored.matches(request)) {
            throw new ProviderRefundConflictException(refundId);
        }
        if (applicationMode.recordsFinancialEffect(createdIdempotencyRecord)) {
            providerFinancialEffects.record(ProviderFinancialEffect.from(stored, Instant.now(clock)));
        }

        return stored.toResponse();
    }

    public ProviderRefundResponse get(UUID refundId) {
        return providerRefundLedger.find(refundId)
                .map(ProviderLedgerEntry::toResponse)
                .orElseThrow(() -> new ProviderRefundNotFoundException(refundId));
    }

    public ProviderRefundApplicationCount countApplications(UUID refundId) {
        return new ProviderRefundApplicationCount(
                refundId,
                providerFinancialEffects.count(refundId));
    }

    private static void validate(UUID refundId, ProviderRefundRequest request) {
        if (refundId == null) {
            throw new InvalidProviderRefundException("refundId is required");
        }
        if (request == null || request.paymentId() == null) {
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

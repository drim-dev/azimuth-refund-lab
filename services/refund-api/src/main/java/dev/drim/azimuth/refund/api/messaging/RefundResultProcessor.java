package dev.drim.azimuth.refund.api.messaging;

import dev.drim.azimuth.Azimuth.ImplementsMechanism;
import dev.drim.azimuth.Azimuth.Realizes;
import dev.drim.azimuth.refund.api.refund.RefundRepository;
import dev.drim.azimuth.refund.contracts.event.RefundEventNames;
import dev.drim.azimuth.refund.contracts.event.RefundOutcome;
import dev.drim.azimuth.refund.contracts.event.RefundResultV1;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefundResultProcessor {
    private final RefundRepository refunds;

    public RefundResultProcessor(RefundRepository refunds) {
        this.refunds = refunds;
    }

    @Transactional
    @Realizes(spec = "refunds/execution", scenario = "interrupted-processing-eventually-completes")
    @ImplementsMechanism(spec = "refunds/execution", mechanism = "idempotent-result-projection")
    public void process(RefundResultV1 event) {
        if (!RefundEventNames.REFUND_RESULT_V1.equals(event.eventType())
                || event.eventVersion() != RefundEventNames.VERSION_1
                || event.outcome() != RefundOutcome.SUCCEEDED) {
            throw new IllegalArgumentException("Unsupported refund result event");
        }
        if (!refunds.recordResultReceipt(event.eventId(), event.refundId(), Instant.now())) {
            return;
        }
        if (!refunds.completeRefund(event.refundId(), event.providerOperationId(), event.appliedAt())) {
            throw new IllegalStateException("Refund result conflicts with refund state: " + event.refundId());
        }
    }
}

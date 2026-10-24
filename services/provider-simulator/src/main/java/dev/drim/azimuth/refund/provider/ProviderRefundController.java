package dev.drim.azimuth.refund.provider;

import static dev.drim.azimuth.refund.contracts.http.RefundHttpPaths.PROVIDER_REFUNDS;
import static dev.drim.azimuth.refund.contracts.http.RefundHttpPaths.PROVIDER_REFUND_BY_KEY;
import static dev.drim.azimuth.refund.contracts.http.RefundHttpPaths.PROVIDER_REFUND_SUMMARY;

import dev.drim.azimuth.refund.contracts.http.ProviderRefundRequest;
import dev.drim.azimuth.refund.contracts.http.ProviderRefundResponse;
import dev.drim.azimuth.refund.contracts.http.ProviderRefundSummary;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
final class ProviderRefundController {
    private final ProviderRefundService providerRefundService;

    ProviderRefundController(ProviderRefundService providerRefundService) {
        this.providerRefundService = providerRefundService;
    }

    @PostMapping(
            path = PROVIDER_REFUNDS,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ProviderRefundResponse> applyRefund(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody ProviderRefundRequest request
    ) {
        return ResponseEntity.ok(providerRefundService.apply(idempotencyKey, request));
    }

    @GetMapping(path = PROVIDER_REFUND_BY_KEY, produces = MediaType.APPLICATION_JSON_VALUE)
    ProviderRefundResponse getRefund(@PathVariable String idempotencyKey) {
        return providerRefundService.get(idempotencyKey);
    }

    @GetMapping(path = PROVIDER_REFUND_SUMMARY, produces = MediaType.APPLICATION_JSON_VALUE)
    ProviderRefundSummary summarizeRefund(@PathVariable UUID refundId) {
        return providerRefundService.summarize(refundId);
    }
}

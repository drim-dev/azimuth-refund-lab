package dev.drim.azimuth.refund.provider;

import static dev.drim.azimuth.refund.contracts.http.RefundHttpPaths.PROVIDER_REFUND_BY_ID;

import dev.drim.azimuth.refund.contracts.http.ProviderRefundRequest;
import dev.drim.azimuth.refund.contracts.http.ProviderRefundResponse;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
final class ProviderRefundController {
    private final ProviderRefundService providerRefundService;

    ProviderRefundController(ProviderRefundService providerRefundService) {
        this.providerRefundService = providerRefundService;
    }

    @PutMapping(
            path = PROVIDER_REFUND_BY_ID,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ProviderRefundResponse> applyRefund(
            @PathVariable UUID refundId,
            @RequestBody ProviderRefundRequest request
    ) {
        return ResponseEntity.ok(providerRefundService.apply(refundId, request));
    }

    @GetMapping(
            path = PROVIDER_REFUND_BY_ID,
            produces = MediaType.APPLICATION_JSON_VALUE)
    ProviderRefundResponse getRefund(@PathVariable UUID refundId) {
        return providerRefundService.get(refundId);
    }

    @GetMapping(
            path = PROVIDER_REFUND_BY_ID + "/applications",
            produces = MediaType.APPLICATION_JSON_VALUE)
    ProviderRefundApplicationCount getApplicationCount(@PathVariable UUID refundId) {
        return providerRefundService.countApplications(refundId);
    }
}

record ProviderRefundApplicationCount(UUID refundId, int count) {
}

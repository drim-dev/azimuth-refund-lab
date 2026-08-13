package dev.drim.azimuth.refund.api.refund;

import dev.drim.azimuth.refund.api.http.ApiExceptionHandler.ApiProblem;
import dev.drim.azimuth.refund.contracts.http.ApproveRefundRequest;
import dev.drim.azimuth.refund.contracts.http.RefundView;
import dev.drim.azimuth.refund.contracts.http.TripFinancialView;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class RefundController {
    private final RefundService refunds;

    public RefundController(RefundService refunds) {
        this.refunds = refunds;
    }

    @PostMapping("/payments/{paymentId}/refunds")
    ResponseEntity<RefundView> approve(
            @PathVariable UUID paymentId,
            @RequestHeader(value = "X-Fixture-Role", required = false) String fixtureRole,
            @RequestBody ApproveRefundRequest request
    ) {
        if (!"support".equals(fixtureRole)) {
            throw new ApiProblem(HttpStatus.UNAUTHORIZED, "Support authentication required",
                    "support-authentication-required", "X-Fixture-Role must identify the support fixture role");
        }
        return ResponseEntity.accepted().body(refunds.approve(paymentId, request));
    }

    @GetMapping("/refunds/{refundId}")
    RefundView getRefund(@PathVariable UUID refundId) {
        return refunds.getRefund(refundId);
    }

    @GetMapping("/trips/{tripId}")
    TripFinancialView getTrip(@PathVariable UUID tripId) {
        return refunds.getTrip(tripId);
    }
}

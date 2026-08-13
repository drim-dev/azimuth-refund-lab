package dev.drim.azimuth.refund.contracts.http;

import java.math.BigDecimal;

public record ApproveRefundRequest(
        BigDecimal amount,
        String currency
) {
}

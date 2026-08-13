package dev.drim.azimuth.refund.contracts.http;

import java.math.BigDecimal;

public record Money(
        BigDecimal amount,
        String currency
) {
}

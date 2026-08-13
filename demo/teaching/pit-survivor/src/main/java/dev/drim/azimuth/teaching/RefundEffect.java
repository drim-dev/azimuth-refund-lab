package dev.drim.azimuth.teaching;

import java.util.HashSet;
import java.util.Set;

final class RefundEffect {
    private final Set<String> appliedRefunds = new HashSet<>();
    private int applicationCount;

    String apply(String refundId) {
        if (appliedRefunds.add(refundId)) {
            applicationCount++;
        }
        return "accepted";
    }

    int applicationCount() {
        return applicationCount;
    }
}

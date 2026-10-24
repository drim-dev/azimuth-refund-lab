package dev.drim.azimuth.teaching;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

final class PaymentProvider {
    private final List<String> attempts = new ArrayList<>();
    private final Set<String> operations = new LinkedHashSet<>();

    String refund(String idempotencyKey) {
        attempts.add(idempotencyKey);
        operations.add(idempotencyKey);
        return "succeeded";
    }

    List<String> attempts() {
        return List.copyOf(attempts);
    }

    int operationCount() {
        return operations.size();
    }
}

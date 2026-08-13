package dev.drim.azimuth.refund.contracts.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RefundOutcome {
    SUCCEEDED("succeeded");

    private final String wireName;

    RefundOutcome(String wireName) {
        this.wireName = wireName;
    }

    @JsonValue
    public String wireName() {
        return wireName;
    }

    @JsonCreator
    public static RefundOutcome fromWireName(String wireName) {
        for (RefundOutcome outcome : values()) {
            if (outcome.wireName.equals(wireName)) {
                return outcome;
            }
        }
        throw new IllegalArgumentException("Unknown refund outcome: " + wireName);
    }
}

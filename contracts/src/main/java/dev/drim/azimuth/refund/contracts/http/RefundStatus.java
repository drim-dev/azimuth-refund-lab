package dev.drim.azimuth.refund.contracts.http;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RefundStatus {
    PENDING("pending"),
    SUCCEEDED("succeeded");

    private final String wireName;

    RefundStatus(String wireName) {
        this.wireName = wireName;
    }

    @JsonValue
    public String wireName() {
        return wireName;
    }

    @JsonCreator
    public static RefundStatus fromWireName(String wireName) {
        for (RefundStatus status : values()) {
            if (status.wireName.equals(wireName)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown refund status: " + wireName);
    }
}

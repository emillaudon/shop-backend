package com.example.shopbackend.demo.order;

import java.util.Arrays;
import java.util.List;

import com.example.shopbackend.demo.common.InvalidStatusException;

public enum Status {
    CREATED,
    PAID,
    SHIPPED,
    CANCELLED;

    public static Status parseStatus(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new InvalidStatusException(raw, allowedValues());
        }

        return Arrays.stream(values())
                .filter(s -> s.name().equalsIgnoreCase(raw))
                .findFirst()
                .orElseThrow(() -> new InvalidStatusException(raw, allowedValues()));
    }

    public boolean canTransitionTo(Status next) {
        return switch (this) {
            case CREATED -> next == PAID || next == CANCELLED;
            case PAID -> next == SHIPPED;
            case SHIPPED, CANCELLED -> false;
        };
    }

    public List<Status> allowedNext() {
        return switch (this) {
            case CREATED -> List.of(PAID, CANCELLED);
            case PAID -> List.of(SHIPPED);
            case SHIPPED, CANCELLED -> List.of();

        };
    }

    public static List<String> allowedValues() {
        return Arrays.stream(values())
                .map(Enum::name)
                .toList();
    }
}

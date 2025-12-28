package com.example.shopbackend.demo.common;

import java.time.LocalDateTime;

public class InvalidDateRangeException extends RuntimeException {
    private final LocalDateTime from;
    private final LocalDateTime to;

    public InvalidDateRangeException(LocalDateTime from, LocalDateTime to) {
        super("From must be before to");
        this.from = from;
        this.to = to;
    }

    public LocalDateTime getFrom() {
        return from;
    }

    public LocalDateTime getTo() {
        return to;
    }

}

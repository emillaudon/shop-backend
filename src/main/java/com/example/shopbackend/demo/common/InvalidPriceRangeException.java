package com.example.shopbackend.demo.common;

public class InvalidPriceRangeException extends RuntimeException {
    private final int from;
    private final int to;

    public InvalidPriceRangeException(int from, int to) {
        super("From must be smaller than to");
        this.from = from;
        this.to = to;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

}

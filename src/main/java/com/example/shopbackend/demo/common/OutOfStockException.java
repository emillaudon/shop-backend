package com.example.shopbackend.demo.common;

public class OutOfStockException extends RuntimeException {
    private final Long productId;
    private final int requested;
    private final int available;

    public OutOfStockException(Long productId, int requested, int available) {
        super("Not enough for product " + productId + " (requested=" + requested + ", available=" + available + ")");
        this.productId = productId;
        this.requested = requested;
        this.available = available;
    }

    public Long getProductId() {
        return productId;
    }

    public int getRequested() {
        return requested;
    }

    public int getAvailable() {
        return available;
    }
}

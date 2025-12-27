package com.example.shopbackend.demo.common;

import java.util.List;

public class InvalidStatusException extends RuntimeException {
    private final String status;
    private final List<String> allowed;

    public InvalidStatusException(String status, List<String> allowed) {
        super("Invalid order status: " + status + ". Allowed values: " + allowed);
        this.status = status;
        this.allowed = allowed;

    }

    public String getStatus() {
        return status;
    }

    public List<String> getAllowed() {
        return allowed;
    }
}

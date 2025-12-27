package com.example.shopbackend.demo.common;

import java.util.List;

import com.example.shopbackend.demo.order.Status;

public class InvalidStatusTransitionException extends RuntimeException {
    private final Status status;
    private final List<Status> allowed;

    public InvalidStatusTransitionException(Status status, Status newStatus, List<Status> allowed) {
        super("Cannot transition to " + newStatus + " from " + status + ". Allowed values: " + allowed);
        this.status = status;
        this.allowed = allowed;

    }

    public Status getStatus() {
        return status;
    }

    public List<Status> getAllowed() {
        return allowed;
    }
}

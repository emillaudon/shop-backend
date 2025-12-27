package com.example.shopbackend.demo.order;

import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
        @NotNull String status) {

}
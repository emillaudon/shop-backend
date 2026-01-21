package com.example.shopbackend.demo.order;

import java.util.List;

import com.example.shopbackend.demo.orderitem.CreateOrderItemRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record CreateOrderRequest(
                @NotEmpty @Valid List<CreateOrderItemRequest> items) {

}

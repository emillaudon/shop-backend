package com.example.shopbackend.demo.product;

public interface ProductStockGateway {
    boolean tryDecreaseStock(Long prouductId, int qty);
}

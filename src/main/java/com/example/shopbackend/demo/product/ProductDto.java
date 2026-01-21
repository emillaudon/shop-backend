package com.example.shopbackend.demo.product;

import io.micrometer.common.lang.Nullable;

public record ProductDto(
        Long id,
        String name,
        int price,
        int stock,
        @Nullable String imageKey) {
    public static ProductDto from(Product product) {
        return new ProductDto(product.getId(), product.getName(), product.getPrice(), product.getStock(), product.getImageKey());
    }
}

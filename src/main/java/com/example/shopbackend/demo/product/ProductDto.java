package com.example.shopbackend.demo.product;

import io.micrometer.common.lang.Nullable;

public record ProductDto(
        Long id,
        String name,
        int price,
        int stock,
        @Nullable String imageUrl) {
    public static ProductDto from(Product product) {
        String imageUrl = product.getImageKey() != null ? "/api/images/" + product.getImageKey() : null;

        return new ProductDto(product.getId(), product.getName(), product.getPrice(), product.getStock(), imageUrl);
    }
}

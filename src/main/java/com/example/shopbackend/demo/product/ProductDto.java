package com.example.shopbackend.demo.product;

public record ProductDto(
        Long id,
        String name,
        int price,
        int stock) {
    public static ProductDto from(Product product) {
        return new ProductDto(product.getId(), product.getName(), product.getPrice(), product.getStock());
    }
}

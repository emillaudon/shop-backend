package com.example.shopbackend.demo.product;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByNameContainingIgnoreCase(String query);

    List<Product> findByStockGreaterThan(int stock);

    List<Product> findByStock(int stock);
}
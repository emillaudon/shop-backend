package com.example.shopbackend.demo.bootstrap;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.example.shopbackend.demo.product.Product;
import com.example.shopbackend.demo.product.ProductRepository;

@Component
public class ProductSeeder implements ApplicationRunner {

    @Value("${SEED_PRODUCTS:false}")
    private boolean seed;

    private final ProductRepository productRepository;

    public ProductSeeder(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!seed)
            return;
        if (productRepository.count() > 0)
            return;

        productRepository.saveAll(List.of(
                new Product("T-shirt Green", 199, 10, "seed-images/green-shirt.png"),
                new Product("Beanie Black", 499, 5, "seed-images/black-beanie.png"),
                new Product("Tote Bag", 149, 20, "seed-images/tote-bag.png"),
                new Product("Beanie Gray", 199, 10, "seed-images/gray-beanie.png"),
                new Product("Belt Brown", 499, 5, "seed-images/brown-belt.png"),
                new Product("Socks White", 149, 20, "seed-images/white-socks.png"),
                new Product("Hat Black", 499, 5, "seed-images/black-hat.png")));

        System.out.println("✅ Seeded products");
    }
}

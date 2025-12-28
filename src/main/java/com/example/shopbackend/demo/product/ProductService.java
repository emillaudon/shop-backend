package com.example.shopbackend.demo.product;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.shopbackend.demo.common.NotFoundException;

import jakarta.transaction.Transactional;

@Service
public class ProductService {
    private final ProductRepository repository;

    public ProductService(final ProductRepository repository) {
        this.repository = repository;
    }

    public Product getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product", id));
    }

    public List<Product> getByName(String query) {
        return repository.findByNameContainingIgnoreCase(query);
    }

    public List<Product> getAll() {
        return repository.findAll();
    }

    public Product create(final String name, int price, int stock) {
        return repository.save(new Product(name, price, stock));
    }

    @Transactional
    public void delete(Long id) {
        Product product = getById(id);
        repository.delete(product);
    }

    @Transactional
    public Product update(Long id, UpdateProductRequest req) {
        Product product = getById(id);

        product.setName(req.name());
        product.setPrice(req.price());
        product.setStock(req.stock());

        repository.save(product);

        return product;
    }
}

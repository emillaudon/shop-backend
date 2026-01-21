package com.example.shopbackend.demo.product;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.shopbackend.demo.common.InvalidPriceRangeException;
import com.example.shopbackend.demo.common.NotFoundException;
import com.example.shopbackend.demo.common.OutOfStockException;
import com.example.shopbackend.demo.storage.ImageStorage;

import jakarta.transaction.Transactional;

@Service
public class ProductService {
    private final ProductRepository repository;
    private final ProductStockGateway productStockGateway;
    private final ImageStorage imageStorage;

    public ProductService(final ProductRepository repository, final ProductStockGateway productStockGateway, final ImageStorage imageStorage) {
        this.repository = repository;
        this.productStockGateway = productStockGateway;
        this.imageStorage = imageStorage;
    }

    public Product getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product", id));
    }

    public List<Product> getByName(String query) {
        return repository.findByNameContainingIgnoreCase(query);
    }

    public List<Product> getInStock(boolean inStock) {
        if (inStock)
            return repository.findByStockGreaterThan(0);

        return repository.findByStock(0);
    }

    @Transactional
    public Product reserveStockOrThrow(Long id, int quantity) {
        boolean ok = productStockGateway.tryDecreaseStock(id, quantity);
        if (!ok) {
            int availableStock = repository.findById(id)
                    .map(Product::getStock)
                    .orElse(0);
            throw new OutOfStockException(id, quantity, availableStock);
        }

        return getById(id);
    }

    public List<Product> getPriceBetween(int from, int to) {
        if (from > to)
            throw new InvalidPriceRangeException(from, to);
        return repository.findByPriceBetween(from, to);
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

    public Product uploadImage(long id, MultipartFile file) {
        if (file.isEmpty()) throw new IllegalArgumentException("Uploaded file is empty");

        String contentType = file.getContentType();
        if (contentType == null) throw new IllegalArgumentException("File has no content type");

        String extension = switch (contentType) {
            case "image/png" -> ".png";
            case "image/jpeg" -> ".jpeg";
            case "image/jpg" -> ".jpg";
            case "image/webp" -> ".webp";
            default -> throw new IllegalArgumentException("Unsupported image type: " + contentType);
        };

        Product product = getById(id);

        String imageKey = "products/" + id + "/main" + extension;

        imageStorage.save(file, imageKey);

        product.setImageKey(imageKey);

        return repository.save(product);
    }
}

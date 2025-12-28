package com.example.shopbackend.demo.order;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.shopbackend.demo.common.InvalidDateRangeException;
import com.example.shopbackend.demo.common.NotFoundException;
import com.example.shopbackend.demo.orderitem.CreateOrderItemRequest;
import com.example.shopbackend.demo.orderitem.OrderItem;
import com.example.shopbackend.demo.product.Product;
import com.example.shopbackend.demo.product.ProductService;
import jakarta.transaction.Transactional;

@Service
public class OrderService {

    private final ProductService productService;
    private final OrderRepository repository;

    public OrderService(ProductService productService, OrderRepository repository) {
        this.productService = productService;
        this.repository = repository;
    }

    public List<Order> getAll() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public Order getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order", id));
    }

    public List<Order> getByStatus(String rawStatus) {
        Status status = Status.parseStatus(rawStatus);
        return repository.findByStatus(status);
    }

    public List<Order> getCreatedBetween(LocalDateTime from, LocalDateTime to) {
        if (from.isAfter(to))
            throw new InvalidDateRangeException(from, to);

        return repository.findByCreatedAtBetween(from, to);
    }

    @Transactional
    public Order updateStatus(Long id, UpdateOrderStatusRequest request) {
        Order order = getById(id);

        Status status = Status.parseStatus(request.status());

        order.changeStatus(status);

        if (status == Status.CANCELLED)
            for (OrderItem orderItem : order.getItems()) {
                Product product = orderItem.getProduct();
                product.increaseStock(orderItem.getQuantity());
            }

        return order;
    }

    @Transactional
    public Order create(CreateOrderRequest request) {
        Order order = new Order();
        for (CreateOrderItemRequest requestItem : request.items()) {
            Product product = productService.getById(requestItem.productId());

            product.decreaseStock(requestItem.quantity());
            OrderItem orderItem = new OrderItem(
                    product, requestItem.quantity(), product.getPrice());
            order.addItem(orderItem);
        }

        return repository.save(order);
    }
}

package com.example.shopbackend.demo.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.shopbackend.demo.common.InvalidDateRangeException;
import com.example.shopbackend.demo.common.InvalidStatusException;
import com.example.shopbackend.demo.common.InvalidStatusTransitionException;
import com.example.shopbackend.demo.common.NotFoundException;
import com.example.shopbackend.demo.orderitem.CreateOrderItemRequest;
import com.example.shopbackend.demo.orderitem.OrderItem;
import com.example.shopbackend.demo.product.Product;
import com.example.shopbackend.demo.product.ProductRepository;
import com.example.shopbackend.demo.product.ProductService;

import jakarta.transaction.Transactional;

@SpringBootTest
@Transactional
public class OrderServiceTest {

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    ProductService productService;

    @Autowired
    ProductRepository productRepository;

    @Test
    void getAll_returnsOrdersSorted() throws InterruptedException {
        Product productOne = new Product("Shirt", 10, 200);
        Product productTwo = new Product("Shirt Also", 11, 201);
        productRepository.save(productOne);
        productRepository.save(productTwo);

        CreateOrderItemRequest orderItemRequestOne = new CreateOrderItemRequest(productOne.getId(), 10);
        CreateOrderItemRequest orderItemRequestTwo = new CreateOrderItemRequest(productTwo.getId(), 20);
        CreateOrderRequest orderRequest = new CreateOrderRequest(List.of(orderItemRequestOne, orderItemRequestTwo));
        orderService.create(orderRequest);
        Thread.sleep(5);

        orderService.create(orderRequest);

        List<Order> orders = orderService.getAll();

        assertTrue(orders.get(0).getCreatedAt().isAfter(orders.get(1).getCreatedAt()), "Expected order with createdAt: "
                + orders.get(1).getCreatedAt() + " to be first but got order with: " + orders.get(0).getCreatedAt());
    }

    @Test
    void getById_existingOrder_returnsOrder() {
        Product product = new Product("Shirt", 10, 10);
        productRepository.save(product);

        Long productId = product.getId();

        CreateOrderItemRequest orderItemRequestOne = new CreateOrderItemRequest(product.getId(), 2);
        CreateOrderRequest orderRequest = new CreateOrderRequest(List.of(orderItemRequestOne));

        Order order = orderService.create(orderRequest);
        Long id = order.getId();
        List<OrderItem> orderItems = order.getItems();
        Order loadedOrder = orderService.getById(id);

        assertEquals(id, loadedOrder.getId());
        assertEquals(orderItems.size(), loadedOrder.getItems().size());
        assertEquals(productId, loadedOrder.getItems().get(0).getProduct().getId());
    }

    @Test
    void getById_missingOrder_throwsNotFoundException() {
        assertThrows(NotFoundException.class, () -> {
            orderService.getById(99999L);
        });
    }

    @Test
    void getByStatus_returnsMatchingOrders() {
        Product productOne = new Product("Shirt", 10, 200);
        Product productTwo = new Product("Shirt Also", 11, 201);
        productRepository.save(productOne);
        productRepository.save(productTwo);

        CreateOrderItemRequest orderItemRequestOne = new CreateOrderItemRequest(productOne.getId(), 10);
        CreateOrderItemRequest orderItemRequestTwo = new CreateOrderItemRequest(productTwo.getId(), 20);

        CreateOrderRequest orderRequest = new CreateOrderRequest(List.of(orderItemRequestOne, orderItemRequestTwo));

        Order orderOne = orderService.create(orderRequest);
        Order orderTwo = orderService.create(orderRequest);
        Order orderThree = orderService.create(orderRequest);
        Order orderFour = orderService.create(orderRequest);

        orderTwo.changeStatus(Status.PAID);
        orderThree.changeStatus(Status.PAID);
        orderThree.changeStatus(Status.SHIPPED);
        orderFour.changeStatus(Status.CANCELLED);

        orderRepository.save(orderOne);
        orderRepository.save(orderTwo);
        orderRepository.save(orderThree);
        orderRepository.save(orderFour);

        List<Order> createdOrders = orderService.getByStatus("CREATED");
        for (Order order : createdOrders) {
            assertEquals(Status.CREATED, order.getStatus());
        }

        List<Order> paidOrders = orderService.getByStatus("PAID");
        for (Order order : paidOrders) {
            assertEquals(Status.PAID, order.getStatus());
        }

        List<Order> shippedOrders = orderService.getByStatus("SHIPPED");
        for (Order order : shippedOrders) {
            assertEquals(Status.SHIPPED, order.getStatus());
        }

        List<Order> cancelledOrders = orderService.getByStatus("CANCELLED");
        for (Order order : cancelledOrders) {
            assertEquals(Status.CANCELLED, order.getStatus());
        }
    }

    @Test
    void getByStatus_misspelledStatus_throwsException() {
        assertThrows(InvalidStatusException.class, () -> {
            orderService.getByStatus("cancl");
        });
    }

    @Test
    void getCreatedBetween_validRange_returnsMatchingOrders() {
        Product product = new Product("Shirt", 10, 200);
        productRepository.save(product);

        CreateOrderItemRequest orderItemRequestOne = new CreateOrderItemRequest(product.getId(), 10);

        CreateOrderRequest orderRequest = new CreateOrderRequest(List.of(orderItemRequestOne));

        Order order = orderService.create(orderRequest);
        Long orderId = order.getId();
        LocalDateTime from = order.getCreatedAt().minusDays(1);
        LocalDateTime to = from.plusDays(100L);

        List<Order> matchingOrders = orderService.getCreatedBetween(from, to);
        assertEquals(1, matchingOrders.size());
        assertEquals(orderId, matchingOrders.get(0).getId());
    }

    @Test
    void getCreatedBetween_fromAfterTo_throwsException() {
        assertThrows(InvalidDateRangeException.class, () -> {
            LocalDateTime from = LocalDateTime.now();
            LocalDateTime to = from.minusDays(100L);

            orderService.getCreatedBetween(from, to);
        });
    }

    @Test
    void updateStatus_validStatus_changesStatus() {
        Product product = new Product("Shirt", 10, 200);
        productRepository.save(product);

        CreateOrderItemRequest orderItemRequestOne = new CreateOrderItemRequest(product.getId(), 10);

        CreateOrderRequest orderRequest = new CreateOrderRequest(List.of(orderItemRequestOne));

        Order order = orderService.create(orderRequest);

        UpdateOrderStatusRequest updateOrderStatusRequest = new UpdateOrderStatusRequest("CANCELLED");

        orderService.updateStatus(order.getId(), updateOrderStatusRequest);

        Order fetchedOrder = orderService.getById(order.getId());

        assertEquals(Status.CANCELLED, fetchedOrder.getStatus());
    }

    @Test
    void updateStatus_validStatus_restocksProducts() {
        Product product = new Product("Shirt", 10, 200);
        productRepository.save(product);
        int originalStock = product.getStock();

        CreateOrderItemRequest orderItemRequestOne = new CreateOrderItemRequest(product.getId(), 10);

        CreateOrderRequest orderRequest = new CreateOrderRequest(List.of(orderItemRequestOne));

        Order order = orderService.create(orderRequest);
        UpdateOrderStatusRequest updateOrderStatusRequest = new UpdateOrderStatusRequest("CANCELLED");

        orderService.updateStatus(order.getId(), updateOrderStatusRequest);

        Product fetchedProduct = productService.getById(product.getId());
        assertEquals(originalStock, fetchedProduct.getStock());
    }

    @Test
    void updateStatus_paidOrShipping_doesNotRestockProducts() {
        Product product = new Product("Shirt", 10, 200);
        productRepository.save(product);
        int originalStock = product.getStock();

        CreateOrderItemRequest orderItemRequestOne = new CreateOrderItemRequest(product.getId(), 10);

        CreateOrderRequest orderRequest = new CreateOrderRequest(List.of(orderItemRequestOne));

        Order order = orderService.create(orderRequest);
        UpdateOrderStatusRequest updateOrderStatusRequest = new UpdateOrderStatusRequest("PAID");

        orderService.updateStatus(order.getId(), updateOrderStatusRequest);

        Product fetchedProduct = productService.getById(product.getId());
        assertEquals(originalStock - 10, fetchedProduct.getStock());

        UpdateOrderStatusRequest updateOrderStatusRequestTwo = new UpdateOrderStatusRequest("SHIPPED");

        orderService.updateStatus(order.getId(), updateOrderStatusRequestTwo);

        Product fetchedProductTwo = productService.getById(product.getId());
        assertEquals(originalStock - 10, fetchedProductTwo.getStock());
    }

    @Test
    void updateStatus_illegalStatusChange_throwsException() {
        Product product = new Product("Shirt", 10, 200);
        productRepository.save(product);

        CreateOrderItemRequest orderItemRequestOne = new CreateOrderItemRequest(product.getId(), 10);

        CreateOrderRequest orderRequest = new CreateOrderRequest(List.of(orderItemRequestOne));

        Order order = orderService.create(orderRequest);

        UpdateOrderStatusRequest updateOrderStatusRequest = new UpdateOrderStatusRequest("SHIPPED");

        assertThrows(InvalidStatusTransitionException.class, () -> {
            orderService.updateStatus(order.getId(), updateOrderStatusRequest);
        });
    }

    @Test
    void updateStatus_missingOrder_throwsNotFoundException() {
        assertThrows(NotFoundException.class,
                () -> orderService.updateStatus(99999L, new UpdateOrderStatusRequest("CANCELLED")));
    }

    @Test
    void cancel_existingOrder_cancelsOrder() {
        Product product = new Product("Shirt", 10, 200);
        productRepository.save(product);

        CreateOrderItemRequest orderItemRequestOne = new CreateOrderItemRequest(product.getId(), 10);

        CreateOrderRequest orderRequest = new CreateOrderRequest(List.of(orderItemRequestOne));

        Order order = orderService.create(orderRequest);

        orderService.cancel(order.getId());

        List<Order> orders = orderService.getByStatus("CANCELLED");

        assertEquals(Status.CANCELLED, orders.get(0).getStatus());
    }

    @Test
    void cancel_existingOrder_restocksProducts() {
        Product product = new Product("Shirt", 10, 200);
        productRepository.save(product);
        int originalStock = product.getStock();

        CreateOrderItemRequest orderItemRequestOne = new CreateOrderItemRequest(product.getId(), 10);

        CreateOrderRequest orderRequest = new CreateOrderRequest(List.of(orderItemRequestOne));

        Order order = orderService.create(orderRequest);

        orderService.cancel(order.getId());

        Product fetchedProduct = productService.getById(product.getId());
        assertEquals(originalStock, fetchedProduct.getStock());
    }

    @Test
    void cancel_missingOrder_throwsNotFoundException() {
        assertThrows(NotFoundException.class, () -> orderService.cancel(99999L));
    }

    @Test
    void create_savesOrderWithCorrectValue() {
        LocalDateTime timeOfTestStart = LocalDateTime.now().minusNanos(1L);
        Product product = new Product("Shirt", 10, 200);
        productRepository.save(product);

        CreateOrderItemRequest orderItemRequestOne = new CreateOrderItemRequest(product.getId(), 10);

        CreateOrderRequest orderRequest = new CreateOrderRequest(List.of(orderItemRequestOne));

        Order order = orderService.create(orderRequest);

        Order fetchedOrder = orderService.getById(order.getId());
        LocalDateTime timeAfterCreation = LocalDateTime.now().plusNanos(1L);
        assertEquals("Shirt", fetchedOrder.getItems().get(0).getProduct().getName());
        assertEquals(10, fetchedOrder.getItems().get(0).getQuantity());
        assertEquals(10, fetchedOrder.getItems().get(0).getUnitPrice());
        assertTrue(fetchedOrder.getId() != null, "id is null.");
        assertTrue(fetchedOrder.getCreatedAt() != null, "Expected time of creation but was null.");
        assertTrue(fetchedOrder.getCreatedAt().isAfter(timeOfTestStart)
                && fetchedOrder.getCreatedAt().isBefore(timeAfterCreation),
                "Expected to be between " + timeOfTestStart + " and " + timeAfterCreation + " but was "
                        + fetchedOrder.getCreatedAt());
    }

    @Test
    void create_removesStockFromProductsCorrectly() {
        Product product = new Product("Shirt", 10, 200);
        productRepository.save(product);
        int originalStock = product.getStock();

        int orderItemQuantity = 10;
        CreateOrderItemRequest orderItemRequestOne = new CreateOrderItemRequest(product.getId(), orderItemQuantity);

        CreateOrderRequest orderRequest = new CreateOrderRequest(List.of(orderItemRequestOne));

        orderService.create(orderRequest);

        Product fetchedOrProduct = productService.getById(product.getId());
        assertEquals(originalStock - orderItemQuantity, fetchedOrProduct.getStock());
    }

    @Test
    void create_multipleItems_savesAllItemsAndCorrectTotal() {
        Product product = new Product("Shirt", 10, 200);
        Product productTwo = new Product("Shirt Also", 10, 200);
        productRepository.save(product);
        productRepository.save(productTwo);

        CreateOrderItemRequest orderItemRequestOne = new CreateOrderItemRequest(product.getId(), 10);
        CreateOrderItemRequest orderItemRequestTwo = new CreateOrderItemRequest(productTwo.getId(), 10);
        int totalValue = (product.getPrice() * 10) + (productTwo.getPrice() * 10);
        BigDecimal totalValuBigDecimal = new BigDecimal(totalValue);

        CreateOrderRequest orderRequest = new CreateOrderRequest(List.of(orderItemRequestOne, orderItemRequestTwo));

        Order order = orderService.create(orderRequest);

        Order fetchedOrder = orderService.getById(order.getId());
        assertEquals(totalValuBigDecimal, fetchedOrder.getTotalValue());
    }
}

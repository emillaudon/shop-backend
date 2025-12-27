package com.example.shopbackend.demo.common;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.HttpStatus;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidStatusTransitionException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleInvalidStatusTransition(InvalidStatusTransitionException ex) {
        return new ApiError(
                "INVALID_STATUS_TRANSITION",
                ex.getMessage(),
                Map.of("status", ex.getStatus(),
                        "allowed", ex.getAllowed()));
    }

    @ExceptionHandler(InvalidStatusException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleInvalidStatus(InvalidStatusException ex) {
        return new ApiError(
                "INVALID_STATUS",
                ex.getMessage(),
                Map.of(
                        "Status", ex.getStatus(),
                        "allowed", ex.getAllowed()));
    }

    @ExceptionHandler(OutOfStockException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleOutOfStock(OutOfStockException ex) {
        return new ApiError(
                "OUT_OF_STOCK",
                ex.getMessage(),
                Map.of(
                        "productId", ex.getProductId(),
                        "requested", ex.getRequested(),
                        "available", ex.getAvailable()));
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(NotFoundException ex) {
        return new ApiError(
                "NOT_FOUND",
                ex.getMessage(),
                Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> fieldErrors = new LinkedHashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(err -> {
            fieldErrors.putIfAbsent(err.getField(), err.getDefaultMessage());
        });

        return new ApiError(
                "VALIDATION_ERROR",
                "Request validation failed",
                fieldErrors);
    }

}

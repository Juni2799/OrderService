package com.ecommerce.order.service;

import com.ecommerce.order.dtos.OrderResponse;

import java.util.Optional;

public interface OrderService {
    Optional<OrderResponse> createOrder(Long userId);
}

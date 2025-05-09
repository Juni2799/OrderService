package com.ecommerce.order.service;

import com.ecommerce.order.dtos.CartItemRequest;
import com.ecommerce.order.models.Cart;

import java.util.List;

public interface CartService {
    boolean addToCart(Long userId, CartItemRequest request);

    boolean deleteItemFromCart(Long userId, Long productId);

    List<Cart> getCartByUser(Long userId);

    void clearCart(Long userId);
}

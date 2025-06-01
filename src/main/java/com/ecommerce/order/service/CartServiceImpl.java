package com.ecommerce.order.service;

import com.ecommerce.order.client.ProductHttpInterface;
import com.ecommerce.order.client.UserHttpInterface;
import com.ecommerce.order.dtos.CartItemRequest;
import com.ecommerce.order.dtos.client.ProductResponse;
import com.ecommerce.order.dtos.client.UserResponse;
import com.ecommerce.order.models.Cart;
import com.ecommerce.order.repository.CartRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
//@Transactional
public class CartServiceImpl implements CartService{
    private final ProductHttpInterface productHttpInterface;
    private final CartRepository cartRepository;
    private final UserHttpInterface userHttpInterface;

    @Override
    public boolean addToCart(Long userId, CartItemRequest request) {
        // look for product
        ProductResponse productResponse = productHttpInterface.getProductById(request.getProductId());
        if(productResponse == null || (productResponse.getStockQuantity() < request.getQuantity())){
            return false;
        }

        // look for user
        UserResponse userResponse = userHttpInterface.getUserById(userId);
        if(userResponse == null) return false;

        Cart existingCart = cartRepository.findByUserIdAndProductId(userId, request.getProductId());
        if(existingCart != null){
            // perform update of existing cart
            existingCart.setQuantity(existingCart.getQuantity() + request.getQuantity());
            //existingCart.setPrice(product.getPrice().multiply(BigDecimal.valueOf(existingCart.getQuantity())));
            existingCart.setPrice(BigDecimal.valueOf(1000)); //Hard-coded for time being, NEED TO FIX LATER
            cartRepository.save(existingCart);
        }else{
            // Create new cart item with requested product
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(request.getProductId());
            cart.setQuantity(request.getQuantity());
            //cart.setPrice(product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity())));
            cart.setPrice(BigDecimal.valueOf(1000)); //Hard-coded for time being, NEED TO FIX LATER
            cartRepository.save(cart);
        }

        return true;
    }

    @Transactional
    @Override
    public boolean deleteItemFromCart(Long userId, Long productId) {
        // look for product
        ProductResponse productResponse = productHttpInterface.getProductById(productId);
        if(productResponse == null){
            return false;
        }
        // look for user
        UserResponse userResponse = userHttpInterface.getUserById(userId);
        if(userResponse == null) return false;

        Cart savedCart = cartRepository.findByUserIdAndProductId(userId, productId);
        if(savedCart != null){
            cartRepository.delete(savedCart);
            return true;
        }

        return false;
    }

    @Override
    public List<Cart> getCartByUser(Long userId) {
        return cartRepository.findByUserId(userId);
    }

    @Transactional
    @Override
    public void clearCart(Long userId) {
        cartRepository.deleteByUserId(userId);
    }
}

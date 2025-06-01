package com.ecommerce.order.client;

import com.ecommerce.order.dtos.client.ProductResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
public interface ProductHttpInterface {
    @GetExchange("/api/v1/products/{productId}")
    ProductResponse getProductById(@PathVariable Long productId);
}

package com.ecommerce.order.client;

import com.ecommerce.order.dtos.client.UserResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
public interface UserHttpInterface {
    @GetExchange("/api/v1/users/{userId}")
    UserResponse getUserById(@PathVariable Long userId);
}

package com.ecommerce.order.service;

import com.ecommerce.order.dtos.OrderItemDTO;
import com.ecommerce.order.dtos.OrderResponse;
import com.ecommerce.order.dtos.events.OrderCreatedEvent;
import com.ecommerce.order.models.Cart;
import com.ecommerce.order.models.Order;
import com.ecommerce.order.models.OrderItem;
import com.ecommerce.order.models.constants.OrderStatus;
import com.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService{
    private final OrderRepository orderRepository;
    //private final UserRepository userRepository;
    private final CartService cartService;
    private final RabbitTemplate rabbitTemplate; //for publishing messages to RabbitMQ

    @Override
    public Optional<OrderResponse> createOrder(Long userId) {
        //Validate cart items
        List<Cart> carts = cartService.getCartByUser(userId);
        if(carts.isEmpty()){
            return Optional.empty();
        }

        //Validate userId
//        Optional<User> userOpt = userRepository.findById(Long.valueOf(userId));
//        if(userOpt.isEmpty()){
//            return Optional.empty();
//        }
//        User user = userOpt.get();

        //Calculate total price
        BigDecimal totalPrice = carts.stream()
                .map(Cart::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        //Finally create order
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setTotalAmount(totalPrice);

        List<OrderItem> orderItems = carts.stream()
                        .map(cart -> new OrderItem(
                                null,
                                cart.getProductId(),
                                cart.getQuantity(),
                                cart.getPrice(),
                                order
                        )).collect(Collectors.toList());
        order.setItems(orderItems);
        Order savedOrder = orderRepository.save(order);

        //Clear the cart as well
        cartService.clearCart(userId);

        //Message to be published after clearing cart
        OrderCreatedEvent orderCreatedEvent = OrderCreatedEvent.builder()
                .orderId(savedOrder.getId())
                .userId(savedOrder.getUserId())
                .orderStatus(savedOrder.getStatus())
                .items(orderItemToOrderItemDTOList(savedOrder.getItems()))
                .totalAmount(savedOrder.getTotalAmount())
                .createdAt(savedOrder.getCreatedAt())
                .build();
        rabbitTemplate.convertAndSend("order.exchange", "order.tracking",
                orderCreatedEvent);

        return Optional.of(orderToOrderResponseMapper(savedOrder));
    }

    private List<OrderItemDTO> orderItemToOrderItemDTOList(List<OrderItem> items){
        return items.stream()
                .map(item -> new OrderItemDTO(
                        item.getId(),
                        item.getProductId(),
                        item.getQuantity(),
                        item.getPrice(),
                        item.getPrice().multiply(new BigDecimal(item.getQuantity()))
                ))
                .collect(Collectors.toList());
    }

    private OrderResponse orderToOrderResponseMapper(Order order){
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        response.setItems(order.getItems().stream()
                .map(orderItem -> new OrderItemDTO(
                        orderItem.getId(),
                        orderItem.getProductId(),
                        orderItem.getQuantity(),
                        orderItem.getPrice(),
                        orderItem.getPrice().multiply(new BigDecimal(orderItem.getQuantity()))
                )).toList());
        response.setCreatedAt(order.getCreatedAt());
        return response;
    }
}

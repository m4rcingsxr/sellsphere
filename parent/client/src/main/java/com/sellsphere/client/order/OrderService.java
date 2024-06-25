package com.sellsphere.client.order;

import com.sellsphere.client.shoppingcart.CartItemRepository;
import com.sellsphere.common.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;

    public Order createOrder(PaymentIntent transaction) {

        Order order = Order.builder()
                .transaction(transaction)
                .orderTime(LocalDateTime.now())
                .build();

        List<CartItem> cart = cartItemRepository.findByCustomer(transaction.getCustomer());
        cart.forEach(order::addOrderDetail);

        order.addOrderTrack(
                OrderTrack.builder()
                        .updatedTime(LocalDate.now())
                        .status(OrderStatus.NEW)
                        .notes(OrderStatus.NEW.getNote())
                        .build()
        );

        return orderRepository.save(order);
    }

}

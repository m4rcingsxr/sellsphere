package com.sellsphere.client.order;

import com.sellsphere.client.shoppingcart.CartItemRepository;
import com.sellsphere.common.entity.CartItem;
import com.sellsphere.common.entity.Order;
import com.sellsphere.common.entity.PaymentIntent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

        return orderRepository.save(order);
    }

}

package com.sellsphere.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order extends IdentifiedEntity {

    @Column(name = "order_time", nullable = false)
    private LocalDateTime orderTime;

    @OneToOne
    @JoinColumn(name = "payment_intent_id", nullable = false)
    private PaymentIntent transaction;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> orderDetails;

    public void addOrderDetail(CartItem cartItem) {
        if(orderDetails == null) {
            orderDetails = new ArrayList<>();
        }

        orderDetails.add(
                OrderDetail.builder()
                        .order(this)
                        .productPrice(cartItem.getProduct().getDiscountPrice())
                        .productCost(cartItem.getProduct().getCost())
                        .product(cartItem.getProduct())
                        .subtotal(cartItem.getSubtotal())
                        .build()
        );
    }

}

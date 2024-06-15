package com.sellsphere.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    @Column(name = "shipping_cost", nullable = false)
    private BigDecimal shippingCost;

    @Column(name = "product_cost", nullable = false)
    private BigDecimal productCost;

    @Column(name = "subtotal", nullable = false)
    private BigDecimal subtotal;

    @Column(name = "tax")
    private BigDecimal tax;

    @Column(name = "total", nullable = false)
    private BigDecimal total;

    @Column(name = "deliver_days", nullable = false)
    private int deliverDays;

    @Column(name = "deliver_date", nullable = false)
    private LocalDate deliverDate;

    @ManyToOne
    @JoinColumn(name = "address_id", nullable = false)
    private Address destinationAddress;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> orderDetails;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "shipment_id")
    private Shipment shipment;

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

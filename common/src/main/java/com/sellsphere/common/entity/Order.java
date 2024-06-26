package com.sellsphere.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
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

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval =
            true)
    @OrderBy("updatedTime ASC, status ASC")
    private List<OrderTrack> orderTracks;

    public void addOrderDetail(CartItem cartItem) {
        if (orderDetails == null) {
            orderDetails = new ArrayList<>();
        }

        orderDetails.add(
                OrderDetail.builder()
                        .order(this)
                        .productPrice(cartItem.getProduct().getDiscountPrice())
                        .productCost(cartItem.getProduct().getCost())
                        .product(cartItem.getProduct())
                        .quantity(cartItem.getQuantity())
                        .subtotal(cartItem.getSubtotal())
                        .build()
        );
    }

    @Transient
    public BigDecimal getProductCost() {
        if(orderDetails != null) {
            return orderDetails.stream().map(OrderDetail::getProductCost).reduce(BigDecimal.ZERO,  BigDecimal::add);
        }
        return BigDecimal.ZERO;
    }

    @Transient
    public boolean isDelivered() {
        return getOrderStatus() == OrderStatus.DELIVERED;
    }

    @Transient
    public boolean isReturnRequested() {
        return getOrderStatus() == OrderStatus.RETURN_REQUESTED;
    }

    @Transient
    public OrderStatus getOrderStatus() {
        if(orderDetails != null && !orderTracks.isEmpty()) {
            List<OrderTrack> orderTracks = this.orderTracks.stream().sorted(Comparator.comparingInt(track -> track.getStatus().ordinal())).toList();
            return orderTracks.get(orderTracks.size() - 1).getStatus();
        }
        return null;
    }


    @Transient
    public LocalDate getEstimatedDeliveryDate() {
        assert transaction.getCourier() != null;
        Integer deliveryDays = transaction.getCourier().getMaxDeliveryTime();

        LocalDate estimatedDate = orderTime.toLocalDate();
        int addedDays = 0;

        while (addedDays < deliveryDays) {
            estimatedDate = estimatedDate.plusDays(1);
            if (!(estimatedDate.getDayOfWeek() == DayOfWeek.SATURDAY || estimatedDate.getDayOfWeek() == DayOfWeek.SUNDAY)) {
                addedDays++;
            }
        }

        return estimatedDate;
    }

    public void addOrderTrack(OrderTrack orderTrack) {
        if (orderTracks == null) {
            orderTracks = new ArrayList<>();
        }

        orderTrack.setOrder(this);
        orderTracks.add(orderTrack);
    }

}

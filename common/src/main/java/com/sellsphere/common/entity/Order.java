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

    @OneToOne(mappedBy = "order", cascade = {CascadeType.MERGE})
    private PaymentIntent transaction;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> orderDetails;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval =
            true, fetch = FetchType.EAGER)
    @OrderBy("updatedTime ASC, status ASC")
    private List<OrderTrack> orderTracks = new ArrayList<>();

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
    public boolean isNew() {
        return containsStatus(OrderStatus.NEW);
    }

    @Transient
    public boolean isCancelled() {
        return containsStatus(OrderStatus.CANCELLED);
    }

    @Transient
    public boolean isProcessing() {
        return containsStatus(OrderStatus.PROCESSING);
    }

    @Transient
    public boolean isPackaged() {
        return containsStatus(OrderStatus.PACKAGED);
    }

    @Transient
    public boolean isPicked() {
        return containsStatus(OrderStatus.PICKED);
    }

    @Transient
    public boolean isShipping() {
        return containsStatus(OrderStatus.SHIPPING);
    }

    @Transient
    public boolean isDelivered() {
        return containsStatus(OrderStatus.DELIVERED);
    }

    @Transient
    public boolean isReturnRequested() {
        return containsStatus(OrderStatus.RETURN_REQUESTED);
    }

    @Transient
    public boolean isReturned() {
        return containsStatus(OrderStatus.RETURNED);
    }

    @Transient
    public boolean isPaid() {
        return containsStatus(OrderStatus.PAID);
    }

    @Transient
    public boolean isRefunded() {
        return containsStatus(OrderStatus.REFUNDED);
    }

    // Helper method to check if a particular status exists in orderTracks
    private boolean containsStatus(OrderStatus status) {
        return orderTracks != null && orderTracks.stream().anyMatch(track -> track.getStatus() == status);
    }

    @Transient
    public OrderStatus getOrderStatus() {
        if (orderTracks != null && !orderTracks.isEmpty()) {
            return orderTracks.stream()
                    .max(Comparator.comparing(OrderTrack::getUpdatedTime)
                                 .thenComparing(track -> track.getStatus().ordinal()))
                    .orElseThrow()
                    .getStatus();
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
        orderTrack.setOrder(this);
        if(orderTracks == null) orderTracks = new ArrayList<>();
        orderTracks.add(orderTrack);
    }

}

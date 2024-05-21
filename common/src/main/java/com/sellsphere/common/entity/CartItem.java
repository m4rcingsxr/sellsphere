package com.sellsphere.common.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "cart_items")
public class CartItem extends IdentifiedEntity {

    @NotNull(message = "Customer is required")
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @NotNull(message = "Product is required")
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @NotNull(message = "Quantity is required")
    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Transient
    private double shippingCost;

    @Transient
    public BigDecimal getSubtotal() {
        return this.product.getDiscountPrice().multiply(BigDecimal.valueOf(quantity));
    }
}

package com.sellsphere.common.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
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

    public CartItem(Integer customerId, Integer productId, Integer quantity) {
        Customer customer = new Customer();
        customer.setId(customerId);
        Product product = new Product();
        product.setId(productId);

        this.customer = customer;
        this.product = product;
        this.quantity = quantity;
    }

    @Transient
    public BigDecimal getSubtotal() {
        return this.product.getDiscountPrice().multiply(BigDecimal.valueOf(quantity));
    }
}

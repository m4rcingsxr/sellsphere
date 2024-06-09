package com.sellsphere.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity class representing a shopping cart.
 * Contains the cart items and is associated with a customer.
 */
@Getter
@Setter
@Entity
@Table(name = "shopping_carts")
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingCart extends IdentifiedEntity {

    /**
     * The payment intent ID associated with the cart.
     */
    @Column(name = "payment_id", unique = true, nullable = true)
    private String paymentIntentId;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItems;

    @OneToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    public void addCartItem(CartItem cartItem) {
        if(cartItems == null){
            cartItems = new ArrayList<>();
        }

        cartItem.setCart(this);
        cartItems.add(cartItem);
    }

    public void removeCartItem(CartItem item) {
        if(cartItems != null) cartItems.remove(item);
    }
}

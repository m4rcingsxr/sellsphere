package com.sellsphere.common.entity;

public class CartItemNotFoundException extends Exception {

    public CartItemNotFoundException() {
        super("Cart item not found.");
    }

    public CartItemNotFoundException(String message) {
        super(message);
    }
}

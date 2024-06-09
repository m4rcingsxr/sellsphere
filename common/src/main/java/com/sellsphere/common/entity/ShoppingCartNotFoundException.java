package com.sellsphere.common.entity;

public class ShoppingCartNotFoundException extends Exception {

    public ShoppingCartNotFoundException() {
        super("Cart not found");
    }

    public ShoppingCartNotFoundException(String message) {
        super(message);
    }
}

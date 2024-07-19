package com.sellsphere.common.entity;

public class PromotionNotFoundException extends Exception{

    public PromotionNotFoundException() {
        super("Promotion not found");
    }

    public PromotionNotFoundException(String message) {
        super(message);
    }
}

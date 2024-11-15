package com.sellsphere.common.entity;

public class BrandNotFoundException extends Exception {

    public BrandNotFoundException() {
        super("Brand not found");
    }

    public BrandNotFoundException(String message) {
        super(message);
    }
}

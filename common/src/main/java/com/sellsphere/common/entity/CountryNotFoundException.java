package com.sellsphere.common.entity;

public class CountryNotFoundException extends Exception {

    public CountryNotFoundException() {
        super("Country not found");
    }

    public CountryNotFoundException(String message) {
        super(message);
    }
}

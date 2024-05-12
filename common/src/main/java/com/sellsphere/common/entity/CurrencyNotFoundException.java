package com.sellsphere.common.entity;

public class CurrencyNotFoundException extends Exception {
    public CurrencyNotFoundException() {
        super("Currency not found");
    }

    public CurrencyNotFoundException(String message) {
        super(message);
    }
}

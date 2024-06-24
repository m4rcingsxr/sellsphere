package com.sellsphere.common.entity;

public class ExchangeRateNotFoundException extends Exception{

    public ExchangeRateNotFoundException() {
        super("Exchange rate not found");
    }

    public ExchangeRateNotFoundException(String message) {
        super(message);
    }
}

package com.sellsphere.common.entity;

public class ChargeNotFoundException extends Exception{

    public ChargeNotFoundException() {
        super("Charge not found");
    }

    public ChargeNotFoundException(String message) {
        super(message);
    }
}

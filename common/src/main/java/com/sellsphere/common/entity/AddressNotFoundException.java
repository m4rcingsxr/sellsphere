package com.sellsphere.common.entity;

public class AddressNotFoundException extends Exception {

    public AddressNotFoundException() {
        super("Address not found");
    }

    public AddressNotFoundException(String message) {
        super(message);
    }
}

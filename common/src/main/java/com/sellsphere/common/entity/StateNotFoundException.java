package com.sellsphere.common.entity;

public class StateNotFoundException extends Exception {
    public StateNotFoundException() {
        super("State not found");
    }

    public StateNotFoundException(String message) {
        super(message);
    }
}

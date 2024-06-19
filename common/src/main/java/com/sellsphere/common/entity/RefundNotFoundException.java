package com.sellsphere.common.entity;

public class RefundNotFoundException extends Exception {

    public RefundNotFoundException() {
        super("Refund not found");
    }

    public RefundNotFoundException(String message) {
        super(message);
    }
}

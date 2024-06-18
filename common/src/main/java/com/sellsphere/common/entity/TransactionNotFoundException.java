package com.sellsphere.common.entity;

public class TransactionNotFoundException extends Exception {
    public TransactionNotFoundException() {
        super("Transaction Not Found");
    }

    public TransactionNotFoundException(String message) {
        super(message);
    }
}

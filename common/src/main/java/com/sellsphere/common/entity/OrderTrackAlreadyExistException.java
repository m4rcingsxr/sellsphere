package com.sellsphere.common.entity;

public class OrderTrackAlreadyExistException extends Exception {
    public OrderTrackAlreadyExistException() {
        super("Order track with this status already exist.");
    }

    public OrderTrackAlreadyExistException(String message) {
        super(message);
    }
}

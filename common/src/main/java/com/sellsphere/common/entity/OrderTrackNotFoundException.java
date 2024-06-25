package com.sellsphere.common.entity;

public class OrderTrackNotFoundException extends Exception{
    public OrderTrackNotFoundException() {
        super("OrderTrack not found");
    }

    public OrderTrackNotFoundException(String message) {
        super(message);
    }
}

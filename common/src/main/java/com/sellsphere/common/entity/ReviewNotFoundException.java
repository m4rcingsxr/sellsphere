package com.sellsphere.common.entity;

public class ReviewNotFoundException extends Throwable {

    public ReviewNotFoundException(){
        super("Review not found.");
    }

    public ReviewNotFoundException(String message) {
        super(message);
    }

}

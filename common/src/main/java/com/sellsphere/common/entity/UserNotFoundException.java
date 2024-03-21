package com.sellsphere.common.entity;

public class UserNotFoundException extends Throwable {

    public UserNotFoundException() {
        super("User not found.");
    }

    public UserNotFoundException(String message) {
        super(message);
    }

}

package com.sellsphere.provider.customer;

import org.keycloak.models.UserModel;

// Add a new CDI event class
public class UserAddedEvent {
    private final UserModel user;

    public UserAddedEvent(UserModel user) {
        this.user = user;
    }

    public UserModel getUser() {
        return user;
    }
}
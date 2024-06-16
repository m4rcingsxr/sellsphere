package com.sellsphere.provider;

import org.keycloak.models.AbstractKeycloakTransaction;
import org.keycloak.models.UserModel;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class UserModelTransaction extends AbstractKeycloakTransaction {

    private final Consumer<UserModel> userConsumer;
    private final Map<String, UserModel> loadedUsers = new HashMap<>();

    public UserModelTransaction(Consumer<UserModel> userConsumer) {
        this.userConsumer = userConsumer;
    }

    public synchronized void addUser(String identifier, UserModel userModel) {
        loadedUsers.put(identifier, userModel);
    }

    public synchronized UserModel findUser(String identifier) {
        return loadedUsers.get(identifier);
    }

    @Override
    protected void commitImpl() {
        loadedUsers.values().forEach(userConsumer);
    }

    @Override
    protected void rollbackImpl() {
        loadedUsers.clear();
    }
}

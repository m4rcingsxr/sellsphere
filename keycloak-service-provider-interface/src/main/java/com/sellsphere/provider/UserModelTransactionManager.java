package com.sellsphere.provider;

import org.keycloak.models.UserModel;

import java.util.function.Consumer;

public class UserModelTransactionManager {

    private static UserModelTransaction userModelTransaction;

    private UserModelTransactionManager() {
        // Private constructor to prevent instantiation
    }

    public static synchronized UserModelTransaction getInstance(Consumer<UserModel> userConsumer) {
        if (userModelTransaction == null) {
            userModelTransaction = new UserModelTransaction(userConsumer);
        }
        return userModelTransaction;
    }

}

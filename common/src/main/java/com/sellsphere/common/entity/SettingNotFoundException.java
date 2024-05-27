package com.sellsphere.common.entity;

public class SettingNotFoundException extends Exception {

    public SettingNotFoundException() {
        super("Setting not found");
    }

    public SettingNotFoundException(String message) {
        super(message);
    }
}

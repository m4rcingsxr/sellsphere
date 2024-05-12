package com.sellsphere.common.entity;

import java.util.List;

public class PaymentSettingManager extends SettingManager {

    public PaymentSettingManager(
            List<Setting> settings) {
        super(settings);
    }

    public String getURL() {
        return super.getSettingValue("PAYPAL_BASE_URL");
    }

    public String getClientID() {
        return super.getSettingValue("PAYPAL_CLIENT_ID");
    }

    public String getClientSecret() {
        return super.getSettingValue("PAYPAL_CLIENT_SECRET");
    }

}
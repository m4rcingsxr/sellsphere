package com.sellsphere.common.entity;

import java.util.Arrays;
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

    public void updateSupportedCountries(String[] countries) {
        String countryCsv = String.join(",", countries);
        super.updateSetting("SUPPORTED_COUNTRY", countryCsv);
    }

    public List<Integer> getSupportedCountries() {
        Setting setting = super.getSetting("SUPPORTED_COUNTRY");
        String countryCsv = setting.getValue();

        return Arrays.stream(countryCsv.split(",")).map(Integer::valueOf).toList();
    }

}
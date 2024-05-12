package com.sellsphere.admin.setting;

import com.sellsphere.common.entity.Setting;
import com.sellsphere.common.entity.SettingManager;

import java.util.List;

public class GeneralSettingManager extends SettingManager {

    public GeneralSettingManager(List<Setting> settings) {
        super(settings);
    }

    public void updateCurrencySymbol(String value) {
        super.updateSetting("CURRENCY_SYMBOL", value);
    }

    public void updateSiteLogo(String value) {
        super.updateSetting("SITE_LOGO", value);
    }

}

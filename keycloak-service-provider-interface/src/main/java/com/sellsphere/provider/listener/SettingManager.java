package com.sellsphere.provider.listener;

import com.sellsphere.provider.customer.external.Setting;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class SettingManager {

    private final List<Setting> settings;

    public Setting get(String key) {
        int index = settings.stream().map(Setting::getKey).toList().indexOf(key);
        if (index != -1) {
            return settings.get(index);
        }
        return null;
    }

    public String getSettingValue(String key) {
        Setting setting = this.get(key);
        return setting != null ? setting.getValue() : null;
    }

    public void update(String key, String value) {
        Setting setting = this.get(key);
        if (setting != null && value != null) {
            setting.setValue(value);
        }
    }


}
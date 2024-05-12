package com.sellsphere.common.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Getter
public abstract class SettingManager {

    private final List<Setting> settings;

    public Setting getSetting(String key) {
        return settings.stream()
                .filter(s -> s.getKey().equals(key))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No such setting: " + key));
    }

    public String getSettingValue(String key) {
        return this.getSetting(key).getValue();
    }

    public void updateSetting(String key, String value) {
        this.getSetting(key).setValue(value);
    }

}

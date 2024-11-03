package com.sellsphere.admin.setting;

import com.sellsphere.common.entity.Setting;
import com.sellsphere.common.entity.SettingManager;

import java.util.List;

/**
 * Manages general application settings, such as updating the currency symbol and site logo.
 * This class extends {@link SettingManager}, which provides core functionality for retrieving and updating settings.
 *
 * <p>This class is primarily used in conjunction with the {@link SettingService} to update general
 * settings in the application.
 *
 * @see SettingManager
 */
public class GeneralSettingManager extends SettingManager {

    /**
     * Constructor that initializes the general setting manager with a list of general settings.
     * These settings include categories like "GENERAL" and "CURRENCY".
     *
     * @param settings the list of settings to manage
     */
    public GeneralSettingManager(List<Setting> settings) {
        super(settings);
    }

    /**
     * Updates the currency symbol setting. This is typically invoked when a new currency is selected
     * in the application's settings form.
     *
     * @param value the new currency symbol (e.g., "$", "€") to be saved in the settings
     */
    public void updateCurrencySymbol(String value) {
        super.updateSetting("CURRENCY_SYMBOL", value);
    }

    /**
     * Updates the site logo setting. This method is typically called after a new logo file is uploaded.
     * The value of this setting is usually the relative path to the logo file.
     *
     * @param value the path or filename of the new site logo
     */
    public void updateSiteLogo(String value) {
        super.updateSetting("SITE_LOGO", value);
    }
}

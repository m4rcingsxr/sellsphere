package com.sellsphere.admin.setting;

import com.sellsphere.common.entity.Setting;
import com.sellsphere.common.entity.SettingCategory;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource("classpath:application-mysql_test.properties")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Sql(scripts = {"classpath:sql/currencies.sql", "classpath:sql/settings.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class SettingRepositoryTest {

    @Autowired
    private SettingRepository settingRepository;

    @Test
    void givenSettingsLoaded_whenFindAll_thenShouldReturnAllSettings() {

        // When
        List<Setting> settings = settingRepository.findAll();

        // Then
        assertNotNull(settings, "The list of settings should not be null");
        assertFalse(settings.isEmpty(), "The list of settings should not be empty");
        assertEquals(24, settings.size(), "The number of settings should be 20");
    }

    @Test
    void givenSettingKeySiteLogo_whenFindById_thenShouldReturnSettingWithKeySiteLogo() {
        // Given
        String key = "SITE_LOGO";

        // When
        Optional<Setting> setting = settingRepository.findById(key);

        // Then
        assertTrue(setting.isPresent(), "Setting with key 'SITE_LOGO' should be present");
        assertEquals("/site-logo/DummyLogo.png", setting.get().getValue(), "Setting value should be '/site-logo/DummyLogo.png'");
    }

    @Test
    void givenNewSetting_whenSave_thenShouldSaveNewSetting() {
        // Given
        Setting setting = new Setting();
        setting.setKey("NEW_SETTING");
        setting.setValue("new_value");
        setting.setCategory(SettingCategory.GENERAL);

        // When
        Setting savedSetting = settingRepository.save(setting);

        // Then
        assertNotNull(savedSetting, "Saved setting should not be null");
        assertNotNull(savedSetting.getKey(), "Saved setting should have a key");
        assertEquals("new_value", savedSetting.getValue(), "Setting value should be 'new_value'");
        assertEquals(SettingCategory.GENERAL, savedSetting.getCategory(), "Setting category should be 'GENERAL'");
    }

    @Test
    void givenSettingKeySiteLogo_whenDeleteById_thenShouldDeleteSettingWithKeySiteLogo() {
        // Given
        String key = "SITE_LOGO";
        assertTrue(settingRepository.existsById(key), "Setting with key 'SITE_LOGO' should exist");

        // When
        settingRepository.deleteById(key);

        // Then
        assertFalse(settingRepository.existsById(key), "Setting with key 'SITE_LOGO' should be deleted");
    }

    @Test
    void givenCategoryGeneral_whenFindAllByCategory_thenShouldReturnAllSettingsInGeneralCategory() {

        // Given
        SettingCategory category = SettingCategory.GENERAL;

        // When
        List<Setting> settings = settingRepository.findAllByCategory(category);

        // Then
        assertNotNull(settings, "The list of settings should not be null");
        assertFalse(settings.isEmpty(), "The list of settings should not be empty");
        assertEquals(3, settings.size(), "The number of settings in 'GENERAL' category should be 3");
    }
}

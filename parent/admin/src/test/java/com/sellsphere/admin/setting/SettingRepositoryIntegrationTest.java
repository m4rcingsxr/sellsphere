package com.sellsphere.admin.setting;

import com.sellsphere.common.entity.Setting;
import com.sellsphere.common.entity.SettingCategory;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Sql(scripts = {"classpath:sql/settings.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class SettingRepositoryIntegrationTest {

    @Autowired
    private SettingRepository settingRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void givenValidSetting_whenSave_thenSettingIsSaved() {
        // Given
        Setting newSetting = new Setting();
        newSetting.setKey("NEW_SETTING");
        newSetting.setValue("New Value");
        newSetting.setCategory(SettingCategory.GENERAL);

        // When
        settingRepository.save(newSetting);

        // Then
        Setting savedSetting = entityManager.find(Setting.class, "NEW_SETTING");
        assertNotNull(savedSetting.getKey(), "Saved setting should have a non-null key");
        assertEquals("New Value", savedSetting.getValue(), "Saved setting should have the correct value");
        assertEquals(SettingCategory.GENERAL, savedSetting.getCategory(), "Saved setting should belong to the GENERAL category");
    }

    @Test
    void givenExistingSetting_whenUpdate_thenSettingIsUpdated() {
        // Given
        String existingSettingKey = "SITE_NAME";
        Optional<Setting> optionalSetting = settingRepository.findById(existingSettingKey);
        assertTrue(optionalSetting.isPresent(), "Existing setting should be present");

        Setting existingSetting = optionalSetting.get();
        existingSetting.setValue("Updated Site Name");

        // When
        Setting updatedSetting = entityManager.find(Setting.class, "SITE_NAME");

        // Then
        assertEquals("Updated Site Name", updatedSetting.getValue(), "The setting value should be updated");
    }

    @Test
    void givenValidSettingKey_whenDelete_thenSettingIsDeleted() {
        // Given
        String settingKey = "SITE_NAME";
        Optional<Setting> settingOptional = settingRepository.findById(settingKey);
        assertTrue(settingOptional.isPresent(), "Setting should exist before deletion");

        // When
        settingRepository.deleteById(settingKey);

        // Then
        Optional<Setting> deletedSetting = settingRepository.findById(settingKey);
        assertTrue(deletedSetting.isEmpty(), "Setting should be deleted");
    }

    @Test
    void givenMultipleSettings_whenSaveAll_thenAllSettingsAreSaved() {
        // Given
        Setting setting1 = new Setting();
        setting1.setKey("NEW_SETTING_1");
        setting1.setValue("Value 1");
        setting1.setCategory(SettingCategory.GENERAL);

        Setting setting2 = new Setting();
        setting2.setKey("NEW_SETTING_2");
        setting2.setValue("Value 2");
        setting2.setCategory(SettingCategory.CURRENCY);

        List<Setting> settingsToSave = List.of(setting1, setting2);

        // When
        List<Setting> savedSettings = settingRepository.saveAll(settingsToSave);

        // Then
        assertEquals(2, savedSettings.size(), "Both settings should be saved");
        assertNotNull(settingRepository.findById("NEW_SETTING_1"), "First setting should be saved and retrievable");
        assertNotNull(settingRepository.findById("NEW_SETTING_2"), "Second setting should be saved and retrievable");
    }

    @Test
    void givenGeneralCategory_whenFindAllByCategory_thenReturnSettings() {
        // Given
        SettingCategory category = SettingCategory.GENERAL;

        // When
        List<Setting> settings = settingRepository.findAllByCategory(category);

        // Then
        assertFalse(settings.isEmpty(), "Settings should be returned for the GENERAL category");
        assertTrue(settings.stream().allMatch(setting -> setting.getCategory().equals(category)),
                "All settings should belong to the GENERAL category");
    }

    @ParameterizedTest(name = "Category: {0}")
    @CsvSource({
            "GENERAL, 3",
            "CURRENCY, 6",
            "MAIL_SERVER, 8",
            "MAIL_TEMPLATES, 4",
            "PAYMENT, 3",
            "SHIPPING, 1"
    })
    void givenDifferentCategories_whenFindAllByCategory_thenReturnMatchingSettings(String categoryName, int expectedCount) {
        // Given
        SettingCategory category = SettingCategory.valueOf(categoryName);

        // When
        List<Setting> settings = settingRepository.findAllByCategory(category);

        // Then
        assertEquals(expectedCount, settings.size(), "The number of settings should match the expected count");
        assertTrue(settings.stream().allMatch(setting -> setting.getCategory().equals(category)),
                "All settings should belong to the " + categoryName + " category");
    }

    @Test
    void givenMultipleCategories_whenFindAllByCategoryIn_thenReturnMatchingSettings() {
        // Given
        List<SettingCategory> categories = List.of(SettingCategory.GENERAL, SettingCategory.CURRENCY);

        // When
        List<Setting> settings = settingRepository.findAllByCategoryIn(categories);

        // Then
        assertFalse(settings.isEmpty(), "Settings should be returned for the given categories");
        assertTrue(settings.stream().allMatch(setting ->
                categories.contains(setting.getCategory())), "All settings should belong to the given categories");
    }

    @ParameterizedTest(name = "Categories: {0}, Expected Total: {1}")
    @CsvSource({
            "GENERAL,CURRENCY, 9",
            "MAIL_SERVER,MAIL_TEMPLATES, 12",
            "PAYMENT,SHIPPING, 4"
    })
    void givenMultipleCategories_whenFindAllByCategoryIn_thenReturnCorrectNumberOfSettings(String category1, String category2, int expectedCount) {
        // Given
        List<SettingCategory> categories = List.of(SettingCategory.valueOf(category1), SettingCategory.valueOf(category2));

        // When
        List<Setting> settings = settingRepository.findAllByCategoryIn(categories);

        // Then
        assertEquals(expectedCount, settings.size(), "The number of settings should match the expected count");
    }
}

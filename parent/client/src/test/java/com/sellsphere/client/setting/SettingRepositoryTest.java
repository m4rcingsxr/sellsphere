package com.sellsphere.client.setting;

import com.sellsphere.common.entity.Setting;
import com.sellsphere.common.entity.SettingCategory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("mysql_test")
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS, scripts = {"classpath:sql/settings.sql"})
class SettingRepositoryTest {

    @Autowired
    private SettingRepository settingRepository;

    @Test
    void givenSettingCategory_whenFindByGeneralCategory_thenReturnCategorySettings() {
        SettingCategory general = SettingCategory.GENERAL;

        List<Setting> settings = settingRepository.findAllByCategory(general);

        assertFalse(settings.isEmpty());
        assertEquals(3, settings.size());
    }

    @Test
    void givenSettingCategories_whenFindByMultipleCategory_thenReturnCategoriesSettings() {
        SettingCategory general = SettingCategory.GENERAL;
        SettingCategory currency = SettingCategory.CURRENCY;

        List<Setting> settings = settingRepository.findAllByCategories(general, currency);

        assertFalse(settings.isEmpty());
        assertEquals(9, settings.size());
    }

    @Test
    void givenSettingKey_whenFindBySettingKey_thenReturnSetting() {
        String key = "SITE_NAME";

        Setting setting = settingRepository.findByKey(key);

        assertNotNull(setting);
        assertEquals(key, setting.getKey());
        assertEquals("DummySite", setting.getValue());
    }

}

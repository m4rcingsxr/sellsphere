package com.sellsphere.admin.setting;

import com.adobe.testing.s3mock.junit5.S3MockExtension;
import com.sellsphere.admin.S3Utility;
import com.sellsphere.common.entity.*;
import com.sellsphere.common.entity.payload.StateDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import software.amazon.awssdk.services.s3.S3Client;
import util.S3TestUtils;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ExtendWith(S3MockExtension.class)
@Sql(scripts = {"classpath:sql/settings.sql", "classpath:sql/currencies.sql", "classpath:sql/countries.sql", "classpath:sql/states.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class SettingServiceIntegrationTest {

    @Autowired
    private SettingService settingService;

    private static final String BUCKET_NAME = "settings-bucket";
    private static S3Client s3Client;

    @BeforeAll
    static void setUpS3(final S3Client client) {
        s3Client = client;
        S3Utility.setBucketName(BUCKET_NAME);
        S3Utility.setS3Client(s3Client);

        S3TestUtils.createBucket(s3Client, BUCKET_NAME);
    }

    @Test
    void whenListAllSettings_thenReturnAllSettings() {
        // When
        List<Setting> settings = settingService.listAllSettings();

        // Then
        assertFalse(settings.isEmpty(), "Settings should be returned");
        assertEquals(25, settings.size(), "There should be 25 settings in total");
    }

    @Test
    void whenListAllCurrencies_thenReturnAllCurrencies() {
        // When
        List<Currency> currencies = settingService.listAllCurrencies();

        // Then
        assertFalse(currencies.isEmpty(), "Currencies should be returned");
        assertEquals(9, currencies.size(), "There should be 10 currencies in total");
    }

    @Test
    void whenListMailTemplateSettings_thenReturnMailTemplateSettings() {
        // When
        List<Setting> settings = settingService.listMailTemplateSettings();

        // Then
        assertFalse(settings.isEmpty(), "Mail Template Settings should be returned");
        assertTrue(settings.stream().allMatch(setting -> setting.getCategory() == SettingCategory.MAIL_TEMPLATES),
                   "All settings should belong to the MAIL_TEMPLATES category");
    }

    @Test
    void whenListStatesByCountry_thenReturnStates() throws Exception {
        // Given
        Integer countryId = 1;

        // When
        List<State> states = settingService.listStatesByCountry(countryId);

        // Then
        assertFalse(states.isEmpty(), "States should be returned");
        assertEquals(9, states.size(), "There should be 3 states for the given country");
    }


    @Test
    void givenValidCategory_whenListGeneralSettings_thenReturnSettings() {
        // When
        List<Setting> settings = settingService.listGeneralSettings();

        // Then
        assertFalse(settings.isEmpty(), "Settings should be returned for the GENERAL and CURRENCY categories");
        assertTrue(settings.stream().allMatch(setting -> 
                setting.getCategory() == SettingCategory.GENERAL || setting.getCategory() == SettingCategory.CURRENCY),
                "Settings should belong to the GENERAL or CURRENCY category");
    }

    @Test
    void givenUploadedFile_whenSaveSiteLogo_thenSaveFileInS3() throws IOException {
        // Given
        MockMultipartFile file = new MockMultipartFile("newImage", "site-logo.jpg", "image/jpeg", "Sample logo".getBytes());

        // When
        settingService.saveSiteLogo(file, new GeneralSettingManager(settingService.listGeneralSettings()));

        // Then
        S3TestUtils.verifyFileContent(s3Client, BUCKET_NAME, "site-logo/site-logo.jpg", file.getInputStream());
    }

    @Test
    void givenSettings_whenSaveSettings_thenSettingsAreUpdated() throws IOException {
        // Given
        MockMultipartFile file = new MockMultipartFile("newImage", "site-logo.jpg", "image/jpeg", "Sample logo".getBytes());

        HttpServletRequest mockRequest = new MockHttpServletRequest();
        ((MockHttpServletRequest) mockRequest).addParameter("CURRENCY_ID", "1");
        ((MockHttpServletRequest) mockRequest).addParameter("SITE_NAME", "UpdatedSite");

        // When
        settingService.save(mockRequest, file);

        // Then
        List<Setting> settings = settingService.listAllSettings();
        Setting siteNameSetting = settings.stream().filter(setting -> "SITE_NAME".equals(setting.getKey())).findFirst().orElse(null);
        assertNotNull(siteNameSetting);
        assertEquals("UpdatedSite", siteNameSetting.getValue(), "The site name should be updated");
    }

    @Test
    void givenCurrencyId_whenUpdateCurrencySymbol_thenCurrencySymbolIsUpdated() {
        // Given
        HttpServletRequest mockRequest = new MockHttpServletRequest();
        ((MockHttpServletRequest) mockRequest).addParameter("CURRENCY_ID", "1");

        GeneralSettingManager generalSettingManager = new GeneralSettingManager(settingService.listGeneralSettings());

        // When
        settingService.updateCurrencySymbol(mockRequest, generalSettingManager);

        // Then
        Setting currencySymbolSetting = generalSettingManager.getSettings().stream()
                .filter(setting -> "CURRENCY_SYMBOL".equals(setting.getKey()))
                .findFirst()
                .orElse(null);

        assertNotNull(currencySymbolSetting);
        assertEquals("$", currencySymbolSetting.getValue(), "The currency symbol should be updated to $");
    }

    @Test
    void givenValidSupportedCountries_whenSaveSupportedCountries_thenCountriesAreSaved() {
        // Given
        List<String> countryIds = List.of("1", "2");

        // When
        settingService.saveSupportedCountries(countryIds);

        // Then
        Setting supportedCountriesSetting = settingService.listPaymentSettings().stream()
                .filter(setting -> "SUPPORTED_COUNTRY".equals(setting.getKey()))
                .findFirst()
                .orElse(null);

        assertNotNull(supportedCountriesSetting);
        assertEquals("1,2", supportedCountriesSetting.getValue(), "Supported countries should be saved correctly");
    }

    @Test
    void givenValidCountryDetails_whenSaveCountry_thenCountryIsSaved() {
        // Given
        Country newCountry = new Country();
        newCountry.setName("TestCountry");
        newCountry.setCode("TC");
        Currency currency = new Currency();
        currency.setId(1);
        newCountry.setCurrency(currency);

        // When
        Country savedCountry = settingService.saveCountry(newCountry);

        // Then
        assertNotNull(savedCountry.getId(), "Country ID should not be null after saving");
        assertEquals("TestCountry", savedCountry.getName(), "Country name should match");
        assertEquals("TC", savedCountry.getCode(), "Country code should match");
    }
    @Test
    void givenCountryWithEmptyName_whenSaveCountry_thenThrowIllegalArgumentException() {
        // Given
        Country newCountry = new Country();
        newCountry.setName("");
        newCountry.setCode("TC");

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> settingService.saveCountry(newCountry));
        assertTrue(exception.getMessage().contains("Country name cannot be empty"), "Exception message should indicate the name cannot be empty");
    }

    @Test
    void givenCountryWithEmptyCode_whenSaveCountry_thenThrowIllegalArgumentException() {
        // Given
        Country newCountry = new Country();
        newCountry.setName("TestCountry");
        newCountry.setCode("");

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> settingService.saveCountry(newCountry));
        assertTrue(exception.getMessage().contains("Country code cannot be empty and must not exceed 3 characters"),
                   "Exception message should indicate the code cannot be empty");
    }

    @Test
    void givenValidStateDetails_whenSaveState_thenStateIsSaved() throws Exception {
        // Given
        StateDTO newStateDTO = new StateDTO();
        newStateDTO.setName("TestState");
        newStateDTO.setCountryId(1);

        // When
        State savedState = settingService.saveState(newStateDTO);

        // Then
        assertNotNull(savedState.getId(), "State ID should not be null after saving");
        assertEquals("TestState", savedState.getName(), "State name should match");
        assertEquals(1, savedState.getCountry().getId(), "Country ID should match the input");
    }

    @Test
    void givenStateWithEmptyName_whenSaveState_thenThrowIllegalArgumentException() {
        // Given
        StateDTO newStateDTO = new StateDTO();
        newStateDTO.setName("");
        newStateDTO.setCountryId(1);

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> settingService.saveState(newStateDTO));
        assertTrue(exception.getMessage().contains("Name is required and cannot exceed 255 characters"),
                   "Exception message should indicate the state name cannot be empty");
    }

    @Test
    void givenStateWithInvalidCountryId_whenSaveState_thenThrowCountryNotFoundException() {
        // Given
        StateDTO newStateDTO = new StateDTO();
        newStateDTO.setName("TestState");
        newStateDTO.setCountryId(999); // Invalid country ID

        // When/Then
        assertThrows(CountryNotFoundException.class, () -> settingService.saveState(newStateDTO));
    }

}

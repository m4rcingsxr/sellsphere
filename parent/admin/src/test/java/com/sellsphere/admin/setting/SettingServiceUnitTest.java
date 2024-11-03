package com.sellsphere.admin.setting;

import com.sellsphere.admin.FileService;
import com.sellsphere.common.entity.*;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class SettingServiceUnitTest {

    @Mock
    private SettingRepository settingRepository;

    @Mock
    private CountryRepository countryRepository;

    @Mock
    private StateRepository stateRepository;

    @Mock
    private CurrencyRepository currencyRepository;

    @InjectMocks
    private SettingService settingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void givenValidFile_whenSaveSiteLogo_thenFileIsSavedInS3() throws IOException {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        GeneralSettingManager generalSettingManager = mock(GeneralSettingManager.class);

        given(file.isEmpty()).willReturn(false);
        given(file.getOriginalFilename()).willReturn("logo.png");

        try (MockedStatic<FileService> mockedFileService = mockStatic(FileService.class)) {
            // When
            settingService.saveSiteLogo(file, generalSettingManager);

            // Then
            mockedFileService.verify(() -> FileService.saveSingleFile(file, "site-logo", "logo.png"));
            verify(generalSettingManager).updateSiteLogo("/site-logo/logo.png");
        }
    }

    @Test
    void givenEmptyFile_whenSaveSiteLogo_thenFileIsNotSaved() throws IOException {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        GeneralSettingManager generalSettingManager = mock(GeneralSettingManager.class);

        given(file.isEmpty()).willReturn(true);

        try (MockedStatic<FileService> mockedFileService = mockStatic(FileService.class)) {
            // When
            settingService.saveSiteLogo(file, generalSettingManager);

            // Then
            mockedFileService.verifyNoInteractions();
            verify(generalSettingManager, never()).updateSiteLogo(anyString());
        }
    }

    @Test
    void givenCurrencySymbolUpdateRequest_whenUpdateCurrencySymbol_thenCurrencySymbolIsUpdated() {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        GeneralSettingManager generalSettingManager = mock(GeneralSettingManager.class);
        given(request.getParameter("CURRENCY_ID")).willReturn("1");

        Currency currency = new Currency();
        currency.setSymbol("$");
        given(currencyRepository.findById(1)).willReturn(Optional.of(currency));

        // When
        settingService.updateCurrencySymbol(request, generalSettingManager);

        // Then
        verify(generalSettingManager).updateCurrencySymbol("$");
    }

    @Test
    void givenInvalidCurrencyId_whenUpdateCurrencySymbol_thenExceptionIsThrown() {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        GeneralSettingManager generalSettingManager = mock(GeneralSettingManager.class);
        given(request.getParameter("CURRENCY_ID")).willReturn("1");

        given(currencyRepository.findById(1)).willReturn(Optional.empty());

        // When/Then
        assertThrows(IllegalStateException.class, () -> settingService.updateCurrencySymbol(request, generalSettingManager));
    }

    @Test
    void givenSupportedCountries_whenSaveSupportedCountries_thenSettingIsUpdated() {
        // Given
        List<String> countryIds = List.of("1", "2");
        Setting setting = new Setting();
        given(settingRepository.save(any(Setting.class))).willReturn(setting);

        // When
        settingService.saveSupportedCountries(countryIds);

        // Then
        verify(settingRepository).save(any(Setting.class));
    }

    @Test
    void givenGeneralSettings_whenListGeneralSettings_thenReturnSettings() {
        // Given
        Setting generalSetting = new Setting();
        generalSetting.setCategory(SettingCategory.GENERAL);

        Setting currencySetting = new Setting();
        currencySetting.setCategory(SettingCategory.CURRENCY);

        given(settingRepository.findAllByCategoryIn(List.of(SettingCategory.GENERAL, SettingCategory.CURRENCY)))
                .willReturn(List.of(generalSetting, currencySetting));

        // When
        List<Setting> settings = settingService.listGeneralSettings();

        // Then
        assertEquals(2, settings.size());
        assertTrue(settings.stream().anyMatch(s -> s.getCategory() == SettingCategory.GENERAL));
        assertTrue(settings.stream().anyMatch(s -> s.getCategory() == SettingCategory.CURRENCY));
    }

    @Test
    void givenMailServerSettings_whenListMailServerSettings_thenReturnSettings() {
        // Given
        Setting mailServerSetting = new Setting();
        mailServerSetting.setCategory(SettingCategory.MAIL_SERVER);

        given(settingRepository.findAllByCategoryIn(List.of(SettingCategory.MAIL_SERVER)))
                .willReturn(List.of(mailServerSetting));

        // When
        List<Setting> settings = settingService.listMailServerSettings();

        // Then
        assertEquals(1, settings.size());
        assertEquals(SettingCategory.MAIL_SERVER, settings.get(0).getCategory());
    }

    @Test
    void givenMailTemplateSettings_whenListMailTemplateSettings_thenReturnSettings() {
        // Given
        Setting mailTemplateSetting = new Setting();
        mailTemplateSetting.setCategory(SettingCategory.MAIL_TEMPLATES);

        given(settingRepository.findAllByCategoryIn(List.of(SettingCategory.MAIL_TEMPLATES)))
                .willReturn(List.of(mailTemplateSetting));

        // When
        List<Setting> settings = settingService.listMailTemplateSettings();

        // Then
        assertEquals(1, settings.size());
        assertEquals(SettingCategory.MAIL_TEMPLATES, settings.get(0).getCategory());
    }

    @Test
    void givenPaymentSettings_whenListPaymentSettings_thenReturnSettings() {
        // Given
        Setting paymentSetting = new Setting();
        paymentSetting.setCategory(SettingCategory.PAYMENT);

        given(settingRepository.findAllByCategoryIn(List.of(SettingCategory.PAYMENT)))
                .willReturn(List.of(paymentSetting));

        // When
        List<Setting> settings = settingService.listPaymentSettings();

        // Then
        assertEquals(1, settings.size());
        assertEquals(SettingCategory.PAYMENT, settings.get(0).getCategory());
    }

    @Test
    void givenValidCountryId_whenFindCountryById_thenReturnCountry() throws CountryNotFoundException {
        // Given
        Country country = new Country();
        given(countryRepository.findById(1)).willReturn(Optional.of(country));

        // When
        Country foundCountry = settingService.findCountryById(1);

        // Then
        assertNotNull(foundCountry);
    }

    @Test
    void givenInvalidCountryId_whenFindCountryById_thenThrowCountryNotFoundException() {
        // Given
        given(countryRepository.findById(1)).willReturn(Optional.empty());

        // When/Then
        assertThrows(CountryNotFoundException.class, () -> settingService.findCountryById(1));
    }

}

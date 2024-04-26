package com.sellsphere.admin.setting;

import com.sellsphere.admin.FileService;
import com.sellsphere.common.entity.Currency;
import com.sellsphere.common.entity.Setting;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class SettingServiceTest {

    @Mock
    private SettingRepository settingRepository;

    @Mock
    private CurrencyRepository currencyRepository;

    @InjectMocks
    private SettingService settingService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private MultipartFile file;

    @Mock
    private GeneralSettingManager generalSettingManager;

    @Test
    void givenCurrencies_whenListAllCurrencies_thenReturnAllCurrencies() {
        // Given
        Currency currency1 = new Currency();
        currency1.setName("USD");
        Currency currency2 = new Currency();
        currency2.setName("EUR");
        when(currencyRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))).thenReturn(
                Arrays.asList(currency1, currency2));

        // When
        List<Currency> currencies = settingService.listAllCurrencies();

        // Then
        assertEquals(2, currencies.size());
        verify(currencyRepository, times(1)).findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    @Test
    void givenSettings_whenListAllSettings_thenReturnAllSettings() {
        // Given
        Setting setting1 = new Setting();
        Setting setting2 = new Setting();
        when(settingRepository.findAll()).thenReturn(Arrays.asList(setting1, setting2));

        // When
        List<Setting> settings = settingService.listAllSettings();

        // Then
        assertEquals(2, settings.size());
        verify(settingRepository, times(1)).findAll();
    }

    @Test
    void givenRequestData_whenSave_thenShouldPersistSettings() throws IOException {
        try (MockedStatic<FileService> mockedFileService = mockStatic(FileService.class)) {

            List<Setting> generalSettings = SettingDataGenerator.generateGeneralSettings();
            List<Setting> templateSettings = SettingDataGenerator.generateMailTemplateSettings();
            List<Setting> mailServerSettings = SettingDataGenerator.generateMailServerSettings();

            List<Setting> allSettings = new ArrayList<>();
            allSettings.addAll(generalSettings);
            allSettings.addAll(templateSettings);
            allSettings.addAll(mailServerSettings);

            Currency currency = new Currency();
            currency.setId(1);
            currency.setName("United States Dollar");
            currency.setSymbol("$");
            currency.setCode("USD");

            when(settingRepository.findAllByCategoryIn(anyList())).thenReturn(allSettings);
            when(file.isEmpty()).thenReturn(false);
            when(file.getOriginalFilename()).thenReturn("logo.png");
            when(currencyRepository.findById(anyInt())).thenReturn(Optional.of(currency));
            when(request.getParameter("CURRENCY_ID")).thenReturn("1");

            doNothing().when(generalSettingManager).updateSiteLogo(anyString());
            doNothing().when(generalSettingManager).updateCurrencySymbol(anyString());

            // Create a spy for SettingService
            SettingService spySettingService = spy(settingService);
            doReturn(generalSettingManager).when(spySettingService).populateGeneralSettingManager();

            spySettingService.save(request, file);

            // Verify that FileService.saveSingleFile was called
            mockedFileService.verify(() -> FileService.saveSingleFile(any(), any(), any()), times(1));

            // Verify that GeneralSettingManager methods were called
            verify(generalSettingManager, times(1)).updateSiteLogo("/site-logo/logo.png");
            verify(generalSettingManager, times(1)).updateCurrencySymbol("$");

            // Verify that settings were saved
            verify(settingRepository, times(3)).saveAll(anyList());
        }
    }


}

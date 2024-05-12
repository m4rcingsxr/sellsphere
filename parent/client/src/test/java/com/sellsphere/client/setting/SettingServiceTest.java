package com.sellsphere.client.setting;

import com.sellsphere.common.entity.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SettingServiceTest {

    @Mock
    private SettingRepository settingRepository;

    @Mock
    private CurrencyRepository currencyRepository;

    @InjectMocks
    private SettingService settingService;

    @Test
    void givenGeneralSettingsExist_whenGetGeneralSettings_thenReturnGeneralSettings() {
        // given
        List<Setting> generalSettings = Arrays.asList(
                new Setting("SITE_LOGO", "/site-logo/DummyLogo.png", SettingCategory.GENERAL),
                new Setting("CURRENCY_ID", "1", SettingCategory.CURRENCY)
        );
        given(settingRepository.findAllByCategories(SettingCategory.GENERAL, SettingCategory.CURRENCY))
                .willReturn(generalSettings);

        // when
        List<Setting> result = settingService.getGeneralSettings();

        // then
        then(result).isEqualTo(generalSettings);
    }

    @Test
    void givenEmailSettingsExist_whenGetEmailSettings_thenReturnEmailSettings() {
        // given
        List<Setting> emailSettings = Arrays.asList(
                new Setting("MAIL_HOST", "smtp.dummy.com", SettingCategory.MAIL_SERVER),
                new Setting("CUSTOMER_EMAIL_VERIFY_SUBJECT", "Please verify your registration", SettingCategory.MAIL_TEMPLATES)
        );
        given(settingRepository.findAllByCategories(SettingCategory.MAIL_SERVER, SettingCategory.MAIL_TEMPLATES))
                .willReturn(emailSettings);

        // when
        List<Setting> result = settingService.getEmailSettings();

        // then
        then(result).isEqualTo(emailSettings);
    }

    @Test
    void givenPaymentSettingsExist_whenGetPaymentSettings_thenReturnPaymentSettings() {
        // given
        List<Setting> paymentSettings = Arrays.asList(
                new Setting("PAYPAL_BASE_URL", "https://api-m.sandbox.paypal.com", SettingCategory.PAYMENT),
                new Setting("PAYPAL_CLIENT_ID", "dummy_client_id", SettingCategory.PAYMENT)
        );
        given(settingRepository.findAllByCategory(SettingCategory.PAYMENT)).willReturn(paymentSettings);

        // when
        PaymentSettingManager result = settingService.getPaymentSettings();

        // then
        then(result.getSettings()).isEqualTo(paymentSettings);
    }

    @Test
    void givenCurrencyExists_whenGetCurrencyCode_thenReturnCurrencyCode() throws CurrencyNotFoundException {
        // given
        Setting currencySetting = new Setting("CURRENCY_ID", "1", SettingCategory.CURRENCY);
        Currency currency = new Currency(1, "US Dollar", "$", "USD");

        given(settingRepository.findByKey("CURRENCY_ID")).willReturn(currencySetting);
        given(currencyRepository.findById(anyInt())).willReturn(Optional.of(currency));

        // when
        String result = settingService.getCurrencyCode();

        // then
        then(result).isEqualTo("USD");
    }

    @Test
    void givenCurrencyDoesNotExist_whenGetCurrencyCode_thenThrowCurrencyNotFoundException() {
        // given
        Setting currencySetting = new Setting("CURRENCY_ID", "999", SettingCategory.CURRENCY);

        given(settingRepository.findByKey("CURRENCY_ID")).willReturn(currencySetting);
        given(currencyRepository.findById(anyInt())).willReturn(Optional.empty());

        // when/then
        thenThrownBy(() -> settingService.getCurrencyCode())
                .isInstanceOf(CurrencyNotFoundException.class);
    }
}

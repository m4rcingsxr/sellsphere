package com.sellsphere.client.setting;

import com.sellsphere.common.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.sellsphere.common.entity.SettingCategory.*;

/**
 * Service class responsible for handling settings related operations.
 * This includes fetching general settings, email settings, payment settings, and currencyconversion information.
 */
@Service
@RequiredArgsConstructor
public class SettingService {

    private final SettingRepository repository;
    private final CurrencyRepository currencyRepository;
    private final CountryRepository countryRepository;

    /**
     * Retrieves general settings.
     *
     * @return a list of general settings
     */
    public List<Setting> getGeneralSettings() {
        return repository.findAllByCategories(GENERAL, CURRENCY);
    }

    /**
     * Retrieves email settings.
     *
     * @return a list of email settings
     */
    public List<Setting> getEmailSettings() {
        return repository.findAllByCategories(MAIL_SERVER, MAIL_TEMPLATES);
    }

    /**
     * Retrieves payment settings.
     *
     * @return a PaymentSettingManager object containing payment settings
     */
    public PaymentSettingManager getPaymentSettings() {
        return new PaymentSettingManager(repository.findAllByCategory(PAYMENT));
    }

    /**
     * Retrieves the currencyconversion code based on the setting.
     *
     * @return the currencyconversion code
     * @throws CurrencyNotFoundException if the currencyconversion is not found
     */
    public String getCurrencyCode() throws CurrencyNotFoundException {
        Setting setting = repository.findByKey("CURRENCY_ID");
        Integer currencyId = Integer.parseInt(setting.getValue());

        Currency currency = currencyRepository.findById(currencyId)
                .orElseThrow(CurrencyNotFoundException::new);

        return currency.getCode();
    }

    public List<Country> getSupportedCountries() {
        List<Setting> paymentSettings = listPaymentSettings();
        PaymentSettingManager paymentManager = new PaymentSettingManager(paymentSettings);

        return countryRepository.findAllById(paymentManager.getSupportedCountries());
    }

    public List<Setting> listPaymentSettings() {
        return repository.findAllByCategory(SettingCategory.PAYMENT);
    }


}

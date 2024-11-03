package com.sellsphere.client.setting;

import com.sellsphere.client.checkout.CurrencyService;
import com.sellsphere.common.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.sellsphere.common.entity.SettingCategory.*;

/**
 * Service class responsible for handling settings related operations.
 * This includes fetching general settings, email settings, payment settings, and exchnagerate information.
 */
@Service
@RequiredArgsConstructor
public class SettingService {

    private final CurrencyService currencyService;
    private final CountryService countryService;

    private final SettingRepository repository;

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
     * Retrieves the exchnagerate code based on the setting.
     *
     * @return the exchnagerate code
     * @throws CurrencyNotFoundException if the exchnagerate is not found
     */
    public String getCurrencyCode(boolean upperCase) throws CurrencyNotFoundException {
        Currency currency = getCurrency();

        return upperCase ? currency.getCode().toUpperCase() : currency.getCode();
    }

    public Currency getCurrency() throws CurrencyNotFoundException {
        Setting setting = repository.findByKey("CURRENCY_ID");
        Integer currencyId = Integer.parseInt(setting.getValue());

        return currencyService.getById(currencyId);
    }

    public List<Country> getSupportedCountries() {
        List<Setting> paymentSettings = listPaymentSettings();
        PaymentSettingManager paymentManager = new PaymentSettingManager(paymentSettings);

        return countryService.getAllById(paymentManager.getSupportedCountries());
    }

    public List<Setting> listPaymentSettings() {
        return repository.findAllByCategory(SettingCategory.PAYMENT);
    }


    public Setting getTaxBehavior() throws SettingNotFoundException {
        return repository.findById("TAX").orElseThrow(SettingNotFoundException::new);
    }

    public String getByKey(String key) {
        return repository.findByKey(key).getValue();
    }
}

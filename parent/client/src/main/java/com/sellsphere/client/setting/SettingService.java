package com.sellsphere.client.setting;

import com.sellsphere.common.entity.Currency;
import com.sellsphere.common.entity.CurrencyNotFoundException;
import com.sellsphere.common.entity.PaymentSettingManager;
import com.sellsphere.common.entity.Setting;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.sellsphere.common.entity.SettingCategory.*;

/**
 * Service class responsible for handling settings related operations.
 * This includes fetching general settings, email settings, payment settings, and currency information.
 */
@Service
@RequiredArgsConstructor
public class SettingService {

    private final SettingRepository repository;
    private final CurrencyRepository currencyRepository;

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
     * Retrieves the currency code based on the setting.
     *
     * @return the currency code
     * @throws CurrencyNotFoundException if the currency is not found
     */
    public String getCurrencyCode() throws CurrencyNotFoundException {
        Setting setting = repository.findByKey("CURRENCY_ID");
        Integer currencyId = Integer.parseInt(setting.getValue());

        Currency currency = currencyRepository.findById(currencyId)
                .orElseThrow(CurrencyNotFoundException::new);

        return currency.getCode();
    }
}

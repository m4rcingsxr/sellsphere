package com.sellsphere.admin.setting;

import com.sellsphere.admin.FileService;
import com.sellsphere.common.entity.*;
import com.sellsphere.common.entity.payload.StateDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Service class responsible for managing system settings. This includes retrieving,
 * updating, and handling file uploads for settings like site logos and other configurations.
 *
 * This class interacts with several repositories such as {@link SettingRepository},
 * {@link CountryRepository}, {@link StateRepository}, and {@link CurrencyRepository} to handle
 * various categories of settings (e.g., General, Payment, Mail).
 */
@RequiredArgsConstructor
@Service
public class SettingService {

    private final SettingRepository settingRepository;
    private final CountryRepository countryRepository;
    private final StateRepository stateRepository;
    private final CurrencyRepository currencyRepository;

    /**
     * Lists all available currencies supported by the system, sorted alphabetically by their names.
     * These currencies are used in different parts of the application, particularly in payment settings.
     *
     * @return a list of all {@link Currency} entities sorted by name in ascending order
     */
    public List<Currency> listAllCurrencies() {
        return currencyRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    /**
     * Retrieves all settings categorized under "CURRENCY". These settings define currency-specific
     * configurations used in the application, such as the default currency symbol.
     *
     * @return a list of {@link Setting} entities categorized under "CURRENCY"
     */
    public List<Setting> getCurrencySettings() {
        return settingRepository.findAllByCategory(SettingCategory.CURRENCY);
    }

    /**
     * Updates the currency symbol in the application's settings based on the selected currency
     * from the form submission.
     *
     * @param request               the HTTP request containing the form data for the selected currency
     * @param generalSettingManager the manager responsible for updating general settings
     * @throws IllegalStateException if the currency with the given ID is not found
     */
     void updateCurrencySymbol(HttpServletRequest request, GeneralSettingManager generalSettingManager) {
        Integer currencyId = Integer.parseInt(request.getParameter("CURRENCY_ID"));
        Currency currency = currencyRepository.findById(currencyId)
                .orElseThrow(() -> new IllegalStateException("Currency not found"));

        generalSettingManager.updateCurrencySymbol(currency.getSymbol());
    }

    /**
     * Retrieves all settings stored in the repository. The settings cover various categories such as
     * general configurations, currency, mail templates, and more.
     *
     * @return a list of all {@link Setting} entities
     */
    public List<Setting> listAllSettings() {
        return settingRepository.findAll();
    }

    /**
     * Retrieves settings categorized as "GENERAL" and "CURRENCY". These settings include the basic
     * configurations such as site logo, site name, and currency symbol.
     *
     * @return a list of settings categorized under "GENERAL" and "CURRENCY"
     */
    public List<Setting> listGeneralSettings() {
        return settingRepository.findAllByCategoryIn(List.of(SettingCategory.GENERAL, SettingCategory.CURRENCY));
    }

    /**
     * Retrieves settings categorized under "MAIL_TEMPLATES". These settings are used for configuring
     * email templates in the application.
     *
     * @return a list of settings under the "MAIL_TEMPLATES" category
     */
    public List<Setting> listMailTemplateSettings() {
        return settingRepository.findAllByCategoryIn(List.of(SettingCategory.MAIL_TEMPLATES));
    }

    /**
     * Retrieves settings categorized under "MAIL_SERVER". These settings configure mail server details
     * like SMTP, port, and email account settings.
     *
     * @return a list of settings under the "MAIL_SERVER" category
     */
    public List<Setting> listMailServerSettings() {
        return settingRepository.findAllByCategoryIn(List.of(SettingCategory.MAIL_SERVER));
    }

    /**
     * Retrieves settings categorized under "PAYMENT". These settings handle payment gateway configurations
     * such as API keys, payment options, and supported currencies.
     *
     * @return a list of settings under the "PAYMENT" category
     */
    public List<Setting> listPaymentSettings() {
        return settingRepository.findAllByCategoryIn(List.of(SettingCategory.PAYMENT));
    }

    /**
     * Retrieves settings categorized under "SHIPPING". These settings configure shipping options
     * such as shipping rates, carriers, and available regions.
     *
     * @return a list of settings under the "SHIPPING" category
     */
    private List<Setting> listShippingSettings() {
        return settingRepository.findAllByCategory(SettingCategory.SHIPPING);
    }

    /**
     * Saves the uploaded site logo file and updates the corresponding "SITE_LOGO" setting.
     * If no file is uploaded, the existing site logo remains unchanged.
     *
     * @param file                  the uploaded logo file (if any)
     * @param generalSettingManager the manager responsible for updating general settings
     * @throws IOException if an error occurs while saving the file
     */
     void saveSiteLogo(MultipartFile file, GeneralSettingManager generalSettingManager) throws IOException {
        if (file != null && !file.isEmpty()) {
            String fileName = file.getOriginalFilename();
            String folderName = "site-logo";

            FileService.saveSingleFile(file, folderName, fileName);
            generalSettingManager.updateSiteLogo("/" + folderName + "/" + fileName);
        }
    }

    /**
     * Saves the settings based on form submissions. This method handles file uploads (such as the site logo)
     * and updates settings related to general configurations, mail, payment, and shipping.
     *
     * @param request the HTTP request containing the form data
     * @param file    the uploaded logo file, if any
     * @throws IOException if an error occurs during file upload
     */
    public void save(HttpServletRequest request, MultipartFile file) throws IOException {
        GeneralSettingManager generalSettingManager = new GeneralSettingManager(listGeneralSettings());

        saveSiteLogo(file, generalSettingManager);
        updateCurrencySymbol(request, generalSettingManager);

        // Update settings in various categories
        updateSettingValuesFromForm(request, generalSettingManager.getSettings());
        updateSettingValuesFromForm(request, listMailServerSettings());
        updateSettingValuesFromForm(request, listMailTemplateSettings());
        updateSettingValuesFromForm(request, listPaymentSettings());
        updateSettingValuesFromForm(request, listShippingSettings());
    }

    /**
     * Updates the values of the provided settings based on form data. Each setting's value is updated
     * from the HTTP request parameters.
     *
     * @param request  the HTTP request containing the form data
     * @param settings the list of settings to update
     */
    private void updateSettingValuesFromForm(HttpServletRequest request, List<Setting> settings) {
        settings.forEach(setting -> {
            String value = request.getParameter(setting.getKey());
            if (value != null) {
                setting.setValue(value);
            }
        });

        settingRepository.saveAll(settings);
    }

    /**
     * Retrieves a list of supported countries based on the payment configuration.
     *
     * @return a list of supported {@link Country} entities
     */
    public List<Country> getSupportedCountries() {
        List<Setting> paymentSettings = listPaymentSettings();
        PaymentSettingManager paymentManager = new PaymentSettingManager(paymentSettings);

        return countryRepository.findAllById(paymentManager.getSupportedCountries());
    }

    /**
     * Saves the supported countries for payment settings based on a list of country IDs.
     *
     * @param countriesId list of country IDs to mark as supported
     */
    public void saveSupportedCountries(List<String> countriesId) {
        String supportedCountries = String.join(",", countriesId);

        Setting setting = new Setting();
        setting.setKey("SUPPORTED_COUNTRY");
        setting.setValue(supportedCountries);
        setting.setCategory(SettingCategory.PAYMENT);

        settingRepository.save(setting);
    }

    /**
     * Saves a country entity to the repository. Validates the country name and code, and throws an
     * {@link IllegalArgumentException} if validation fails.
     *
     * @param country the country entity to save
     * @return the saved {@link Country} entity
     */
    public Country saveCountry(Country country) throws CountryNotFoundException {
        String errorMessage = "";
        if (country.getName().isEmpty() || country.getName().length() > 64) {
            errorMessage = "Country name cannot be empty and must not exceed 64 characters.";
        }

        if (country.getCode().isEmpty() || country.getCode().length() > 3) {
            errorMessage += " Country code cannot be empty and must not exceed 3 characters.";
        }

        if (!errorMessage.isEmpty()) {
            throw new IllegalArgumentException(errorMessage);
        }

        Country existingCountry = findCountryById(country.getId());
        country.setCurrency(existingCountry.getCurrency());

        return countryRepository.save(country);
    }

    /**
     * Deletes a country by its ID. Throws a {@link CountryNotFoundException} if the country does not exist.
     *
     * @param countryId the ID of the country to delete
     * @throws CountryNotFoundException if the country is not found
     */
    public void deleteCountry(Integer countryId) throws CountryNotFoundException {
        Country country = countryRepository.findById(countryId)
                .orElseThrow(CountryNotFoundException::new);
        countryRepository.delete(country);
    }

    /**
     * Lists all countries stored in the repository, sorted alphabetically by name.
     *
     * @return a list of all {@link Country} entities sorted by name in ascending order
     */
    public List<Country> listAllCountries() {
        return countryRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    /**
     * Retrieves all states associated with a given country, sorted alphabetically by state name.
     *
     * @param countryId the ID of the country whose states are to be listed
     * @return a list of {@link State} entities for the given country
     * @throws CountryNotFoundException if the country is not found
     */
    public List<State> listStatesByCountry(Integer countryId) throws CountryNotFoundException {
        Country country = findCountryById(countryId);
        return stateRepository.findAllByCountry(country, Sort.by(Sort.Direction.ASC, "name"));
    }

    /**
     * Finds a country by its ID. Throws a {@link CountryNotFoundException} if the country is not found.
     *
     * @param countryId the ID of the country to find
     * @return the {@link Country} entity if found
     * @throws CountryNotFoundException if the country is not found
     */
    public Country findCountryById(Integer countryId) throws CountryNotFoundException {
        return countryRepository.findById(countryId)
                .orElseThrow(CountryNotFoundException::new);
    }

    /**
     * Saves a state entity to the repository based on the provided DTO. Validates the state's name
     * and country ID, and throws an {@link IllegalArgumentException} if validation fails.
     *
     * @param stateDTO the state data transfer object to save
     * @return the saved {@link State} entity
     * @throws CountryNotFoundException if the country associated with the state is not found
     */
    public State saveState(StateDTO stateDTO) throws CountryNotFoundException {
        String errorMessage = "";
        if (stateDTO.getCountryId() == null) {
            errorMessage += "Country ID must be present.";
        }

        if (stateDTO.getName().isEmpty() || stateDTO.getName().length() > 255) {
            errorMessage += "Name is required and cannot exceed 255 characters.";
        }

        if (!errorMessage.isEmpty()) {
            throw new IllegalArgumentException(errorMessage);
        }

        Country country = countryRepository.findById(stateDTO.getCountryId())
                .orElseThrow(CountryNotFoundException::new);

        State state = new State();
        state.setId(stateDTO.getId());
        state.setName(stateDTO.getName());
        state.setCountry(country);

        return stateRepository.save(state);
    }

    /**
     * Deletes a state by its ID. Throws a {@link StateNotFoundException} if the state is not found.
     *
     * @param id the ID of the state to delete
     * @throws StateNotFoundException if the state is not found
     */
    public void deleteState(Integer id) throws StateNotFoundException {
        State state = stateRepository.findById(id)
                .orElseThrow(StateNotFoundException::new);
        stateRepository.delete(state);
    }
}

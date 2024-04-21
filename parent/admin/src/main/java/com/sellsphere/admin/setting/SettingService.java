package com.sellsphere.admin.setting;

import com.sellsphere.admin.FileService;
import com.sellsphere.common.entity.Currency;
import com.sellsphere.common.entity.Setting;
import com.sellsphere.common.entity.SettingCategory;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Service
public class SettingService {

    private final SettingRepository settingRepository;

    private final CurrencyRepository currencyRepository;

    public List<Currency> listAllCurrencies() {
        return currencyRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    public List<Setting> listAllSettings() {
        return settingRepository.findAll();
    }

    public List<Setting> listGeneralSettings() {
        return settingRepository.findAllByCategoryIn(List.of(SettingCategory.GENERAL, SettingCategory.CURRENCY));
    }

    public List<Setting> listMailTemplateSettings() {
        return settingRepository.findAllByCategoryIn(List.of(SettingCategory.MAIL_TEMPLATES));
    }

    public List<Setting> listMailServerSettings() {
        return settingRepository.findAllByCategoryIn(List.of(SettingCategory.MAIL_SERVER));
    }

    public void save(HttpServletRequest request, MultipartFile file) throws IOException {
        GeneralSettingManager generalSettingManager = populateGeneralSettingManager();

        saveSiteLogo(file, generalSettingManager);
        updateCurrencySymbol(request, generalSettingManager);

        updateSettingValuesFromForm(request, generalSettingManager.getSettings());
        updateSettingValuesFromForm(request, listMailServerSettings());
        updateSettingValuesFromForm(request, listMailTemplateSettings());
    }

    protected GeneralSettingManager populateGeneralSettingManager() {
        return new GeneralSettingManager(listGeneralSettings());
    }

    private void saveSiteLogo(MultipartFile file, GeneralSettingManager generalSettingManager)
            throws IOException {
        if (file != null && !file.isEmpty()) {
            String fileName = file.getOriginalFilename();
            String folderName = "site-logo";

            FileService.saveSingleFile(file, folderName, fileName);
            generalSettingManager.updateSiteLogo("/" + folderName + "/" + fileName);
        }
    }

    private void updateCurrencySymbol(HttpServletRequest request,
                                      GeneralSettingManager generalSettingManager) {
        Integer currencyId = Integer.parseInt(request.getParameter("CURRENCY_ID"));
        Currency currency = currencyRepository.findById(currencyId).orElseThrow(
                () -> new IllegalStateException("Currency not found"));

        generalSettingManager.updateCurrencySymbol(currency.getSymbol());
    }

    private void updateSettingValuesFromForm(HttpServletRequest request, List<Setting> settings) {
        settings.forEach(setting -> {
            String value = request.getParameter(setting.getKey());
            if (value != null) setting.setValue(value);
        });

        settingRepository.saveAll(settings);
    }

}

package com.sellsphere.admin.setting;

import com.sellsphere.common.entity.Currency;
import com.sellsphere.common.entity.Setting;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class SettingService {

    private final SettingRepository settingRepository;

    private final CurrencyRepository currencyRepository;

    public List<Setting> listAllSettings() {
        return settingRepository.findAll();
    }

    public List<Currency> listAllCurrencies() {
        return currencyRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }



}

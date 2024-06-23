package com.sellsphere.client.checkout;

import com.sellsphere.client.setting.CurrencyRepository;
import com.sellsphere.common.entity.Currency;
import com.sellsphere.common.entity.CurrencyNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrencyService {

    private final CurrencyRepository currencyRepository;

    public Currency getByCode(String currencyCode) throws CurrencyNotFoundException {
        return currencyRepository.findByCode(currencyCode).orElseThrow(CurrencyNotFoundException::new);
    }

    public Currency getById(Integer currencyId) throws CurrencyNotFoundException {
        return currencyRepository.findById(currencyId).orElseThrow(CurrencyNotFoundException::new);
    }
}

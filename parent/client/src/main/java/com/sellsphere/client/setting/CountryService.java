package com.sellsphere.client.setting;

import com.sellsphere.common.entity.Country;
import com.sellsphere.common.entity.CountryNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CountryService {

    private final CountryRepository countryRepository;

    public Country getCountryByCode(String code) throws CountryNotFoundException {
        return countryRepository.findByCode(code).orElseThrow(CountryNotFoundException::new);
    }


    public List<Country> getAllById(List<Integer> supportedCountries) {
        return countryRepository.findAllById(supportedCountries);
    }
}

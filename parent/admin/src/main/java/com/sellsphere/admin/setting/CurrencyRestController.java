package com.sellsphere.admin.setting;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CurrencyRestController {

    private final CurrencyRepository currencyRepository;

    // available currencies for selected supported countries for shipment and taxes
    @PostMapping("/currencies")
    public ResponseEntity<List<CurrencyDto>> findByCountries(@RequestBody List<Integer> countries) {

        List<CurrencyDto> currenciesByCountryId = currencyRepository
                .findAllByCountryIds(countries)
                .stream()
                .map(CurrencyDto::new)
                .toList();

        return ResponseEntity.ok(currenciesByCountryId);
    }

}

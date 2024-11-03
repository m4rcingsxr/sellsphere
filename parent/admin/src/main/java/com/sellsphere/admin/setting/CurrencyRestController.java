package com.sellsphere.admin.setting;

import com.sellsphere.common.entity.payload.CurrencyDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * CurrencyRestController is a REST controller responsible for handling currency-related operations.
 * It provides an endpoint to fetch a list of currencies based on the IDs of the countries.
 *
 * <p>This controller interacts with the {@link CurrencyRepository} to query currencies associated
 * with specific countries. The result is returned as a list of {@link CurrencyDTO} objects.</p>
 */
@RestController
@RequiredArgsConstructor
public class CurrencyRestController {

    private final CurrencyRepository currencyRepository;

    /**
     * Retrieves a list of currencies based on the provided country IDs.
     *
     * <p>This method accepts a list of country IDs in the request body and fetches the corresponding
     * currencies using {@link CurrencyRepository#findAllByCountryIds}. Each currency is mapped to a
     * {@link CurrencyDTO} for the response.</p>
     *
     * @param countries a list of country IDs for which currencies are to be fetched.
     * @return a {@link ResponseEntity} containing the list of {@link CurrencyDTO} objects.
     */
    @PostMapping("/currencies")
    public ResponseEntity<List<CurrencyDTO>> findByCountries(@RequestBody List<Integer> countries) {

        List<CurrencyDTO> currenciesByCountryId = currencyRepository
                .findAllByCountryIds(countries)
                .stream()
                .map(CurrencyDTO::new)
                .toList();

        return ResponseEntity.ok(currenciesByCountryId);
    }

}
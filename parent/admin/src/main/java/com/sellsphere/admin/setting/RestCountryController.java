package com.sellsphere.admin.setting;

import com.sellsphere.common.entity.Country;
import com.sellsphere.common.entity.CountryNotFoundException;
import com.sellsphere.common.entity.Setting;
import com.sellsphere.common.entity.SettingCategory;
import com.sellsphere.common.entity.payload.CountryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * RestCountryController is a REST controller for managing Country entities.
 * It provides endpoints to list, delete, and save countries.
 */
@RequiredArgsConstructor
@RestController
public class RestCountryController {

    private final CountryRepository countryRepository;
    private final SettingRepository settingRepository;

    private final SettingService settingService;


    /**
     * Lists all countries sorted by name in ascending order.
     *
     * @return ResponseEntity containing the list of CountryDTOs.
     */
    @GetMapping("/countries/list")
    public ResponseEntity<List<CountryDTO>> listAll() {
        List<CountryDTO> countries = countryRepository
                .findAll(Sort.by(Sort.Direction.ASC, "name"))
                .stream()
                .map(CountryDTO::new)
                .toList();

        return ResponseEntity.ok(countries);
    }

    /**
     * Deletes a country by its ID.
     *
     * @param countryId the ID of the country to be deleted.
     * @return ResponseEntity with HTTP status OK.
     * @throws CountryNotFoundException if the country with the specified ID is not found.
     */
    @GetMapping("/countries/delete/{countryId}")
    public ResponseEntity<Void> delete(@PathVariable("countryId") Integer countryId)
            throws CountryNotFoundException {
        Country country = countryRepository
                .findById(countryId)
                .orElseThrow(CountryNotFoundException::new);

        countryRepository.delete(country);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Saves a new country or updates an existing one.
     *
     * @param country the country entity to be saved.
     * @return ResponseEntity containing the saved CountryDTO.
     */
    @PostMapping("/countries/save")
    public ResponseEntity<CountryDTO> save(@RequestBody Country country) {
        String errorMessage = "";
        if(country.getName().isEmpty() || country.getCode().length() > 64) {
            errorMessage = "Country name can not be empty and exceed 64 characters.";
        }

        if(country.getCode().isEmpty() || country.getCode().length() > 3) {
            errorMessage += "Country code can not be empty and exceed 3 characters.";
        }

        if(!errorMessage.isEmpty()) {
            throw new IllegalArgumentException(errorMessage);
        }

        Country savedCountry = countryRepository.save(country);
        return ResponseEntity.ok(new CountryDTO(savedCountry));
    }

    @PostMapping("/countries/support")
    public ResponseEntity<Void> setAsSupported(@RequestBody List<String> countriesId) {
        String supportedCountries = String.join(",", countriesId);

        Setting setting = new Setting();
        setting.setKey("SUPPORTED_COUNTRY");
        setting.setValue(supportedCountries);
        setting.setCategory(SettingCategory.PAYMENT);

        settingRepository.save(setting);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/countries/support")
    public ResponseEntity<List<CountryDTO>> getSupportedCountries() {
        List<CountryDTO> supportedCountries = settingService.getSupportedCountries().stream().map(
                CountryDTO::new).toList();

        return ResponseEntity.ok(supportedCountries);
    }
}

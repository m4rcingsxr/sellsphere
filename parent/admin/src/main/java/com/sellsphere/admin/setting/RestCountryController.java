package com.sellsphere.admin.setting;

import com.sellsphere.common.entity.Country;
import com.sellsphere.common.entity.CountryNotFoundException;
import com.sellsphere.common.entity.payload.CountryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * RestCountryController is a REST controller for managing {@link Country} entities.
 * It provides endpoints to list, delete, and save countries, as well as manage supported countries
 * in the application's payment settings.
 */
@RequiredArgsConstructor
@RestController
public class RestCountryController {

    private final SettingService settingService;

    /**
     * Lists all countries sorted by name in ascending order.
     *
     * <p>This method retrieves all countries from the {@link CountryRepository} and returns them as
     * {@link CountryDTO} objects in a sorted list. The sorting is based on the name of the
     * country.</p>
     *
     * @return a {@link ResponseEntity} containing the list of {@link CountryDTO}s.
     */
    @GetMapping("/countries/list")
    public ResponseEntity<List<CountryDTO>> listAll() {
        List<CountryDTO> countries = settingService.listAllCountries()
                .stream()
                .map(CountryDTO::new)
                .toList();

        return ResponseEntity.ok(countries);
    }

    /**
     * Deletes a country by its ID.
     *
     * <p>If the country with the specified ID is not found, a {@link CountryNotFoundException}
     * is thrown.
     * Otherwise, the country is deleted from the repository.</p>
     *
     * @param countryId the ID of the country to be deleted.
     * @return a {@link ResponseEntity} with HTTP status OK if the deletion is successful.
     * @throws CountryNotFoundException if the country with the specified ID is not found.
     */
    @DeleteMapping("/countries/delete/{countryId}")
    public ResponseEntity<Void> delete(@PathVariable("countryId") Integer countryId)
            throws CountryNotFoundException {
        settingService.deleteCountry(countryId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Saves a new country or updates an existing one.
     *
     * <p>This method validates the provided {@link Country} entity, ensuring that the name and
     * code fields meet the required constraints. If the validation passes, the country is saved in the
     * repository. If validation fails, an {@link IllegalArgumentException} is thrown.</p>
     *
     * @param country the {@link Country} entity to be saved.
     * @return a {@link ResponseEntity} containing the saved {@link CountryDTO}.
     */
    @PostMapping("/countries/save")
    public ResponseEntity<CountryDTO> save(@RequestBody Country country) throws CountryNotFoundException {
        Country savedCountry = settingService.saveCountry(country);
        return ResponseEntity.ok(new CountryDTO(savedCountry));
    }


    /**
     * Sets countries as supported for payment.
     *
     * <p>This method accepts a list of country IDs and marks them as supported countries in
     * the application's payment settings.</p>
     *
     * @param countriesId the list of country IDs to be marked as supported.
     * @return an empty {@link ResponseEntity} with HTTP status NO_CONTENT.
     */
    @PostMapping("/countries/support")
    public ResponseEntity<Void> setAsSupported(@RequestBody List<String> countriesId) {
        settingService.saveSupportedCountries(countriesId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Retrieves the list of supported countries for payment.
     *
     * <p>This method fetches the supported countries from the settings and returns them as
     * {@link CountryDTO} objects. The {@link SettingService} is used to retrieve the supported countries.</p>
     *
     * @return a {@link ResponseEntity} containing the list of supported countries.
     */
    @GetMapping("/countries/support")
    public ResponseEntity<List<CountryDTO>> getSupportedCountries() {
        List<CountryDTO> supportedCountries = settingService.getSupportedCountries()
                .stream()
                .map(CountryDTO::new)
                .toList();

        return ResponseEntity.ok(supportedCountries);
    }
}
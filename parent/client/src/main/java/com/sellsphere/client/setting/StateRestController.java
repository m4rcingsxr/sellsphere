package com.sellsphere.client.setting;

import com.sellsphere.common.entity.Country;
import com.sellsphere.common.entity.CountryNotFoundException;
import com.sellsphere.common.entity.StateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class StateRestController {

    private final CountryRepository countryRepository;

    /**
     * Retrieves a list of states based on a given country ID.
     *
     * @param countryId the ID of the country to fetch states for
     * @return a list of states belonging to the specified country
     */
    @GetMapping("/states/list_by_country/{countryId}")
    public ResponseEntity<List<StateDTO>> listStates(@PathVariable("countryId") Integer countryId)
            throws CountryNotFoundException {
        if(true) {
            throw new CountryNotFoundException();
        }
        Country country = countryRepository.findById(countryId).orElseThrow(CountryNotFoundException::new);;

        List<StateDTO> states = country.getStates().stream().map(
                StateDTO::new).toList();

        return ResponseEntity.ok(states);
    }

}


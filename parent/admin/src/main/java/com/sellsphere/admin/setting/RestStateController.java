package com.sellsphere.admin.setting;

import com.sellsphere.common.entity.Country;
import com.sellsphere.common.entity.CountryNotFoundException;
import com.sellsphere.common.entity.State;
import com.sellsphere.common.entity.StateNotFoundException;
import com.sellsphere.common.entity.payload.StateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class RestStateController {

    private final CountryRepository countryRepository;
    private final StateRepository stateRepository;

    @GetMapping("/states/by_country/{countryId}")
    public ResponseEntity<List<StateDTO>> listStates(@PathVariable("countryId") Integer countryId)
            throws CountryNotFoundException {
        Country country = countryRepository.findById(countryId).orElseThrow(
                CountryNotFoundException::new);

        List<StateDTO> states = stateRepository.findAllByCountry(country,
                                                                 Sort.by(Sort.Direction.ASC, "name")
        ).stream().map(StateDTO::new).toList();
        return ResponseEntity.ok(states);
    }

    @PostMapping("/states/save")
    public ResponseEntity<StateDTO> save(@RequestBody StateDTO stateDTO)
            throws CountryNotFoundException {

        String errorMessage = "";
        if(stateDTO.getCountryId() == null) {
            errorMessage += "Country id must be present.";
        }

        if(stateDTO.getName().length() > 255 || stateDTO.getName().isEmpty()) {
            errorMessage += "Name is required and can not exceed 255 characters.";
        }

        if(!errorMessage.isEmpty()) {
            throw new IllegalArgumentException(errorMessage);
        }


        Country country = countryRepository
                .findById(stateDTO.getCountryId())
                .orElseThrow(CountryNotFoundException::new);

        State state = new State();
        state.setId(stateDTO.getId());
        state.setName(stateDTO.getName());
        state.setCountry(country);

        State savedState = stateRepository.save(state);
        return ResponseEntity.status(HttpStatus.OK).body(new StateDTO(savedState));
    }

    @DeleteMapping("/states/delete/{id}")
    public ResponseEntity<Void> deleteState(@PathVariable("id") Integer id)
            throws StateNotFoundException {
        State state = stateRepository.findById(id).orElseThrow(StateNotFoundException::new);
        stateRepository.delete(state);

        return new ResponseEntity<>(HttpStatus.OK);
    }

}

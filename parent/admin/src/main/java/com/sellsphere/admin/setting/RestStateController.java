package com.sellsphere.admin.setting;

import com.sellsphere.common.entity.CountryNotFoundException;
import com.sellsphere.common.entity.State;
import com.sellsphere.common.entity.StateNotFoundException;
import com.sellsphere.common.entity.payload.StateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * RestStateController manages REST endpoints for state-related operations.
 * It provides methods to list, save, and delete states associated with countries.
 *
 * <p>This controller interacts with {@link SettingService} to perform the necessary business
 * logic for state management.</p>
 */
@RequiredArgsConstructor
@RestController
public class RestStateController {

    private final SettingService settingService;

    /**
     * Lists all states by a given country ID.
     *
     * <p>This method retrieves the list of states associated with the provided country ID
     * and returns them as a list of {@link StateDTO} objects.</p>
     *
     * @param countryId the ID of the country for which states are to be listed.
     * @return a ResponseEntity containing the list of states.
     * @throws CountryNotFoundException if the country is not found.
     */
    @GetMapping("/states/by_country/{countryId}")
    public ResponseEntity<List<StateDTO>> listStates(@PathVariable("countryId") Integer countryId)
            throws CountryNotFoundException {
        List<State> states = settingService.listStatesByCountry(countryId);
        return ResponseEntity.ok(
                states.stream().map(StateDTO::new).toList()
        );
    }

    /**
     * Saves a new or updated state.
     *
     * <p>This method receives a {@link StateDTO} object representing the state to be saved,
     * validates it, and passes it to the {@link SettingService} for persistence.</p>
     *
     * @param stateDTO the state data to be saved.
     * @return a ResponseEntity containing the saved state.
     * @throws CountryNotFoundException if the associated country is not found.
     */
    @PostMapping("/states/save")
    public ResponseEntity<StateDTO> save(@RequestBody StateDTO stateDTO)
            throws CountryNotFoundException {
        State savedState = settingService.saveState(stateDTO);
        return ResponseEntity.status(HttpStatus.OK).body(new StateDTO(savedState));
    }

    /**
     * Deletes a state by its ID.
     *
     * <p>This method deletes the state with the specified ID and returns an HTTP status OK
     * response upon success.</p>
     *
     * @param id the ID of the state to be deleted.
     * @return an empty ResponseEntity with HTTP status OK.
     * @throws StateNotFoundException if the state is not found.
     */
    @DeleteMapping("/states/delete/{id}")
    public ResponseEntity<Void> deleteState(@PathVariable("id") Integer id)
            throws StateNotFoundException {
        settingService.deleteState(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}

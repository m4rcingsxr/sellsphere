package com.sellsphere.admin.setting;

import com.sellsphere.common.entity.Country;
import com.sellsphere.common.entity.CountryNotFoundException;
import com.sellsphere.common.entity.State;
import com.sellsphere.common.entity.StateNotFoundException;
import com.sellsphere.common.entity.payload.StateDTO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class RestStateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SettingService settingService;

    @Test
    void givenValidCountryId_whenListStates_thenReturnStates() throws Exception {
        // Given
        Integer countryId = 1;
        Country country = new Country();
        country.setId(countryId);
        country.setName("United States");

        State state1 = new State();
        state1.setId(1);
        state1.setName("California");
        state1.setCountry(country);

        State state2 = new State();
        state2.setId(2);
        state2.setName("Texas");
        state2.setCountry(country);

        List<State> states = List.of(state1, state2);

        given(settingService.listStatesByCountry(countryId)).willReturn(states);

        // When / Then
        mockMvc.perform(get("/states/by_country/{countryId}", countryId)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("California"))
                .andExpect(jsonPath("$[1].name").value("Texas"));

        then(settingService).should().listStatesByCountry(countryId);
    }

    @Test
    void givenInvalidCountryId_whenListStates_thenThrowCountryNotFoundException() throws Exception {
        // Given
        Integer invalidCountryId = 999;
        given(settingService.listStatesByCountry(invalidCountryId))
                .willThrow(new CountryNotFoundException());

        // When / Then
        mockMvc.perform(get("/states/by_country/{countryId}", invalidCountryId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        then(settingService).should().listStatesByCountry(invalidCountryId);
    }

    @Test
    void givenValidStateDTO_whenSaveState_thenReturnSavedState() throws Exception {
        // Given
        StateDTO stateDTO = new StateDTO();
        stateDTO.setName("California");
        stateDTO.setCountryId(1);

        Country country = new Country();
        country.setId(1);
        country.setName("United States");

        State savedState = new State();
        savedState.setId(1);
        savedState.setName("California");
        savedState.setCountry(country);

        given(settingService.saveState(ArgumentMatchers.any(StateDTO.class))).willReturn(savedState);

        // When / Then
        mockMvc.perform(post("/states/save")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\": \"California\", \"countryId\": 1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("California"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.countryId").value(1)); // Assert that the countryId is returned

        then(settingService).should().saveState(ArgumentMatchers.any(StateDTO.class));
    }

    @Test
    void givenInvalidCountryId_whenSaveState_thenThrowCountryNotFoundException() throws Exception {
        // Given
        StateDTO stateDTO = new StateDTO();
        stateDTO.setName("Invalid State");
        stateDTO.setCountryId(999);

        given(settingService.saveState(ArgumentMatchers.any(StateDTO.class)))
                .willThrow(new CountryNotFoundException());

        // When / Then
        mockMvc.perform(post("/states/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Invalid State\", \"countryId\": 999}"))
                .andExpect(status().isNotFound());

        then(settingService).should().saveState(ArgumentMatchers.any(StateDTO.class));
    }

    @Test
    void givenValidStateId_whenDeleteState_thenReturnOk() throws Exception {
        // Given
        Integer stateId = 1;

        // When / Then
        mockMvc.perform(delete("/states/delete/{id}", stateId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        then(settingService).should().deleteState(stateId);
    }

    @Test
    void givenInvalidStateId_whenDeleteState_thenThrowStateNotFoundException() throws Exception {
        // Given
        Integer invalidStateId = 999;
        doThrow(new StateNotFoundException()).when(settingService).deleteState(invalidStateId);

        // When / Then
        mockMvc.perform(delete("/states/delete/{id}", invalidStateId)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        then(settingService).should().deleteState(invalidStateId);
    }

}

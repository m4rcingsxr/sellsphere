package com.sellsphere.admin.setting;

import com.sellsphere.common.entity.Country;
import com.sellsphere.common.entity.State;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class RestStateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CountryRepository countryRepository;

    @MockBean
    private StateRepository stateRepository;

    @Test
    void givenStateDTO_whenSave_thenReturnSavedState() throws Exception {
        Country country = new Country();
        country.setId(1);
        country.setName("USA");
        country.setCode("US");

        State state = new State();
        state.setId(1);
        state.setName("California");
        state.setCountry(country);

        when(countryRepository.findById(anyInt())).thenReturn(Optional.of(country));
        when(stateRepository.save(any(State.class))).thenReturn(state);

        String stateJson = "{\"id\":1,\"name\":\"California\",\"countryId\":1}";

        mockMvc.perform(post("/states/save").contentType(MediaType.APPLICATION_JSON).content(
                stateJson)).andExpect(status().isOk()).andExpect(
                jsonPath("$.id").value(1)).andExpect(
                jsonPath("$.name").value("California")).andExpect(jsonPath("$.countryId").value(1));

        verify(countryRepository, times(1)).findById(1);
        verify(stateRepository, times(1)).save(any(State.class));
    }

    @Test
    void givenInvalidCountryId_whenSave_thenThrowCountryNotFoundException() throws Exception {
        when(countryRepository.findById(anyInt())).thenReturn(Optional.empty());

        String stateJson = "{\"id\":1,\"name\":\"California\",\"countryId\":999}";

        mockMvc.perform(post("/states/save").contentType(MediaType.APPLICATION_JSON).content(
                stateJson)).andExpect(status().isNotFound());

        verify(countryRepository, times(1)).findById(999);
        verify(stateRepository, times(0)).save(any(State.class));
    }

    @Test
    void givenStateId_whenDeleteState_thenReturnStatusOk() throws Exception {
        State state = new State();
        state.setId(1);
        state.setName("California");

        when(stateRepository.findById(anyInt())).thenReturn(Optional.of(state));

        mockMvc.perform(delete("/states/delete/1")).andExpect(status().isOk());

        verify(stateRepository, times(1)).findById(1);
        verify(stateRepository, times(1)).delete(any(State.class));
    }

    @Test
    void givenInvalidStateId_whenDeleteState_thenThrowStateNotFoundException() throws Exception {
        when(stateRepository.findById(anyInt())).thenReturn(Optional.empty());

        mockMvc.perform(delete("/states/delete/999")).andExpect(status().isNotFound());

        verify(stateRepository, times(1)).findById(999);
        verify(stateRepository, times(0)).delete(any(State.class));
    }
}

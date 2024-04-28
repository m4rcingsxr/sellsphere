package com.sellsphere.client.setting;

import com.sellsphere.common.entity.Country;
import com.sellsphere.common.entity.CustomerNotFoundException;
import com.sellsphere.common.entity.State;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static com.sellsphere.client.address.AddressTestUtil.generateDummyCountry;
import static java.nio.file.Files.exists;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class StateRestControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CountryRepository countryRepository;

    @Test
    void givenExistingCountryId_whenGetStatesByCountry_thenListOfStatesIsReturned()
            throws Exception {
        int countryId = 1;
        Country country = generateDummyCountry();
        State state = new State();
        country.addState(state);

        when(countryRepository.findById(countryId)).thenReturn(Optional.of(country));

        mvc.perform(get("/states/list_by_country/{countryId}", countryId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)));

        verify(countryRepository, times(1)).findById(countryId);
    }

    @Test
    void givenNotExistingCountryId_whenGetStatesByCountry_thenCountryNotFounfExceptionIsThrown()
            throws Exception {
        when(countryRepository.findById(-1)).thenReturn(Optional.empty());

        mvc.perform(get("/states/list_by_country/{countryId}", -1))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value("404"));
    }

}
package com.sellsphere.admin.setting;

import com.sellsphere.common.entity.Country;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class RestCountryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CountryRepository countryRepository;

    @Test
    void givenCountries_whenListAll_thenReturnCountries() throws Exception {
        Country country1 = new Country();
        country1.setId(1);
        country1.setName("USA");
        country1.setCode("US");
        Country country2 = new Country();
        country2.setId(2);
        country2.setName("Poland");
        country2.setCode("PL");

        when(countryRepository.findAll(any(Sort.class)))
                .thenReturn(Arrays.asList(country1, country2));

        mockMvc.perform(get("/countries/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("USA"))
                .andExpect(jsonPath("$[0].code").value("US"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Poland"))
                .andExpect(jsonPath("$[1].code").value("PL"));

        verify(countryRepository, times(1)).findAll(any(Sort.class));
    }

    @Test
    void givenCountryId_whenDeleteCountry_thenReturnStatusOk() throws Exception {
        Country country = new Country();
        country.setId(1);
        country.setName("USA");
        country.setCode("US");

        when(countryRepository.findById(anyInt())).thenReturn(Optional.of(country));

        mockMvc.perform(get("/countries/delete/1"))
                .andExpect(status().isOk());

        verify(countryRepository, times(1)).findById(1);
        verify(countryRepository, times(1)).delete(country);
    }

    @Test
    void givenCountry_whenSaveCountry_thenReturnSavedCountry() throws Exception {
        Country country = new Country();
        country.setId(1);
        country.setName("USA");
        country.setCode("US");

        when(countryRepository.save(any(Country.class))).thenReturn(country);

        mockMvc.perform(post("/countries/save")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"id\":1,\"name\":\"USA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("USA"));

        verify(countryRepository, times(1)).save(any(Country.class));
    }
}

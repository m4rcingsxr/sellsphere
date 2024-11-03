package com.sellsphere.admin.setting;

import com.sellsphere.common.entity.Country;
import com.sellsphere.common.entity.Currency;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class RestCountryControllerTest {

    @MockBean
    private SettingService settingService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void whenListAllCountries_thenReturnAllCountries() throws Exception {
        // Given
        Currency eur = new Currency();
        eur.setCode("EUR");
        eur.setSymbol("€");

        Country country1 = Country.builder()
                .name("Austria")
                .code("AT")
                .currency(eur)
                .build();

        Country country2 = Country.builder()
                .name("Belgium")
                .code("BE")
                .currency(eur)
                .build();

        given(settingService.listAllCountries()).willReturn(List.of(country1, country2));

        // When/Then
        mockMvc.perform(get("/countries/list")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Austria"))
                .andExpect(jsonPath("$[0].currencyCode").value("EUR"))
                .andExpect(jsonPath("$[1].name").value("Belgium"))
                .andExpect(jsonPath("$[1].currencySymbol").value("€"));

        then(settingService).should().listAllCountries();
    }

    @Test
    void givenValidCountryId_whenDeleteCountry_thenReturnOk() throws Exception {
        // When/Then
        mockMvc.perform(delete("/countries/delete/{countryId}", 1)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        then(settingService).should().deleteCountry(1);
    }

    @Test
    void givenValidCountry_whenSaveCountry_thenReturnSavedCountry() throws Exception {
        // Given
        Currency usd = new Currency();
        usd.setId(1);
        usd.setCode("USD");
        usd.setSymbol("$");

        Country country = new Country();
        country.setId(1);
        country.setName("United States");
        country.setCode("US");
        country.setCurrency(usd);

        given(settingService.saveCountry(any(Country.class))).willReturn(country);

        // When/Then
        mockMvc.perform(post("/countries/save")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\": \"United States\", \"code\": \"US\", \"currency\": {\"code\": \"USD\", \"symbol\": \"$\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("United States"))
                .andExpect(jsonPath("$.code").value("US"))
                .andExpect(jsonPath("$.currencyCode").value("USD"))
                .andExpect(jsonPath("$.currencySymbol").value("$"));

        // Verify
        then(settingService).should().saveCountry(any(Country.class));
    }


    @Test
    void givenValidCountryIds_whenSetAsSupported_thenReturnNoContent() throws Exception {
        // When/Then
        mockMvc.perform(post("/countries/support")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("[1, 2]"))
                .andExpect(status().isNoContent());

        then(settingService).should().saveSupportedCountries(List.of("1", "2"));
    }
}

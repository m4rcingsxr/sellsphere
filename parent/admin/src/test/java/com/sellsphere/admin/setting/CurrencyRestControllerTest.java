package com.sellsphere.admin.setting;

import com.sellsphere.common.entity.Currency;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class CurrencyRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CurrencyRepository currencyRepository;

    @Test
    void givenValidCountryIds_whenFindByCountries_thenReturnCurrencyDTOList() throws Exception {
        // Given
        List<Integer> countryIds = List.of(1, 2);

        Currency currency1 = new Currency();
        currency1.setCode("USD");
        currency1.setSymbol("$");

        Currency currency2 = new Currency();
        currency2.setCode("EUR");
        currency2.setSymbol("€");

        List<Currency> currencies = List.of(currency1, currency2);

        given(currencyRepository.findAllByCountryIds(countryIds)).willReturn(currencies);

        // When / Then
        mockMvc.perform(post("/currencies")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("[1, 2]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("USD"))
                .andExpect(jsonPath("$[0].symbol").value("$"))
                .andExpect(jsonPath("$[1].code").value("EUR"))
                .andExpect(jsonPath("$[1].symbol").value("€"));

        // Verify
        then(currencyRepository).should().findAllByCountryIds(countryIds);
    }

    @Test
    void givenEmptyCountryIds_whenFindByCountries_thenReturnEmptyList() throws Exception {
        // Given
        List<Integer> emptyCountryIds = List.of();

        given(currencyRepository.findAllByCountryIds(emptyCountryIds)).willReturn(List.of());

        // When / Then
        mockMvc.perform(post("/currencies")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("[]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        // Verify
        then(currencyRepository).should().findAllByCountryIds(emptyCountryIds);
    }

    @Test
    void givenInvalidCountryIds_whenFindByCountries_thenReturnEmptyList() throws Exception {
        // Given
        List<Integer> invalidCountryIds = List.of(999, 888);

        given(currencyRepository.findAllByCountryIds(invalidCountryIds)).willReturn(List.of());

        // When / Then
        mockMvc.perform(post("/currencies")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("[999, 888]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        // Verify
        then(currencyRepository).should().findAllByCountryIds(invalidCountryIds);
    }
}

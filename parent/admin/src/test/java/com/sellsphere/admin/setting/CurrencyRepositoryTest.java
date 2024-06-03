package com.sellsphere.admin.setting;

import com.sellsphere.common.entity.Currency;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Sql(scripts = {"classpath:sql/currencies.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class CurrencyRepositoryTest {

    @Autowired
    private CurrencyRepository currencyRepository;

    @Test
    void givenCurrenciesLoaded_whenFindAll_thenShouldReturnAllCurrencies() {

        // When
        List<Currency> currencies = currencyRepository.findAll();

        // Then
        assertNotNull(currencies, "The list of currencies should not be null");
        assertFalse(currencies.isEmpty(), "The list of currencies should not be empty");
        assertEquals(13, currencies.size(), "The number of currencies should be 13");
    }

    @Test
    void givenCurrencyId1_whenFindById_thenShouldReturnCurrencyWithId1() {
        // Given
        int id = 1;

        // When
        Optional<Currency> currency = currencyRepository.findById(id);

        // Then
        assertTrue(currency.isPresent(), "Currency with ID 1 should be present");
        assertEquals("United States Dollar", currency.get().getName(), "Currency name should be United States Dollar");
    }

    @Test
    void givenNewCurrency_whenSave_thenShouldSaveNewCurrency() {
        // Given
        Currency currency = new Currency();
        currency.setName("com.sellsphere.admin.Test Currency");
        currency.setSymbol("T$");
        currency.setCode("TST");

        // When
        Currency savedCurrency = currencyRepository.save(currency);

        // Then
        assertNotNull(savedCurrency, "Saved currency_conversion should not be null");
        assertNotNull(savedCurrency.getId(), "Saved currency_conversion should have an ID");
        assertEquals("com.sellsphere.admin.Test Currency", savedCurrency.getName(), "Currency name should be 'com.sellsphere.admin.Test Currency'");
        assertEquals("T$", savedCurrency.getSymbol(), "Currency symbol should be 'T$'");
        assertEquals("TST", savedCurrency.getCode(), "Currency code should be 'TST'");
    }

    @Test
    void givenCurrencyId1_whenDeleteById_thenShouldDeleteCurrencyWithId1() {
        // Given
        int id = 1;
        assertTrue(currencyRepository.existsById(id), "Currency with ID 1 should exist");

        // When
        currencyRepository.deleteById(id);

        // Then
        assertFalse(currencyRepository.existsById(id), "Currency with ID 1 should be deleted");
    }

    @Test
    void givenCurrenciesLoaded_whenFindAllSortedByName_thenShouldReturnAllCurrenciesSortedByName() {

        // When
        List<Currency> currencies = currencyRepository.findAll(Sort.by("name"));

        // Then
        assertNotNull(currencies, "The list of currencies should not be null");
        assertFalse(currencies.isEmpty(), "The list of currencies should not be empty");
        assertEquals(13, currencies.size(), "The number of currencies should be 13");
        assertEquals("Australian Dollar", currencies.get(0).getName(), "The first currency_conversion should be 'Australian Dollar'");
        assertEquals("Vietnamese đồng", currencies.get(currencies.size() - 1).getName(), "The last currency_conversion should be 'Vietnamese đồng'");
    }
}

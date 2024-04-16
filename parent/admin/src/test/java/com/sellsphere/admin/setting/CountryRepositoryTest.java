package com.sellsphere.admin.setting;

import com.sellsphere.common.entity.Country;
import com.sellsphere.common.entity.State;
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
@Sql(scripts = {"classpath:sql/countries.sql", "classpath:sql/states.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class CountryRepositoryTest {

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private StateRepository stateRepository;

    @Test
    void givenCountriesLoaded_whenFindAll_thenShouldReturnAllCountries() {

        // When
        List<Country> countries = countryRepository.findAll();

        // Then
        assertNotNull(countries, "The list of countries should not be null");
        assertFalse(countries.isEmpty(), "The list of countries should not be empty");
        assertEquals(5, countries.size(), "The number of countries should be 5");
    }

    @Test
    void givenCountryId1_whenFindById_thenShouldReturnCountryWithId1() {

        // Given
        int id = 1;

        // When
        Optional<Country> country = countryRepository.findById(id);

        // Then
        assertTrue(country.isPresent(), "Country with ID 1 should be present");
        assertEquals("Afghanistan", country.get().getName(), "Country name should be Afghanistan");
    }

    @Test
    void givenNewCountry_whenSave_thenShouldSaveNewCountry() {

        // Given
        Country country = new Country();
        country.setName("Test Country");
        country.setCode("TST");

        // When
        Country savedCountry = countryRepository.save(country);

        // Then
        assertNotNull(savedCountry, "Saved country should not be null");
        assertNotNull(savedCountry.getId(), "Saved country should have an ID");
        assertEquals("Test Country", savedCountry.getName(), "Country name should be 'Test Country'");
        assertEquals("TST", savedCountry.getCode(), "Country code should be 'TST'");
    }

    @Test
    void givenCountryId1_whenDeleteById_thenShouldDeleteCountryWithId1() {

        // Given
        int id = 1;
        assertTrue(countryRepository.existsById(id), "Country with ID 1 should exist");

        // When
        countryRepository.deleteById(id);

        // Then
        assertFalse(countryRepository.existsById(id), "Country with ID 1 should be deleted");
    }

    @Test
    void givenCountriesLoaded_whenFindAllSortedByName_thenShouldReturnAllCountriesSortedByName() {

        // When
        List<Country> countries = countryRepository.findAll(Sort.by("name"));

        // Then
        assertNotNull(countries, "The list of countries should not be null");
        assertFalse(countries.isEmpty(), "The list of countries should not be empty");
        assertEquals(5, countries.size(), "The number of countries should be 5");
        assertEquals("Afghanistan", countries.get(0).getName(), "The first country should be 'Afghanistan'");
        assertEquals("Angola", countries.get(countries.size() - 1).getName(), "The last country should be 'Angola'");
    }

    @Test
    void givenCountryAndNewState_whenAddState_thenShouldVerifyStateIsCreated() {

        // Given
        Optional<Country> optionalCountry = countryRepository.findById(4);
        assertTrue(optionalCountry.isPresent(), "Country with ID 1 should be present");
        Country country = optionalCountry.get();

        State newState = new State();
        newState.setName("New State");

        // When
        country.addState(newState);
        countryRepository.save(country);

        // Then
        Optional<State> foundState = stateRepository.findById(country.getStates().iterator().next().getId());
        assertTrue(foundState.isPresent(), "New state should be present");
        assertEquals("New State", foundState.get().getName(), "State name should be 'New State'");
        assertEquals(country.getId(), foundState.get().getCountry().getId(), "State should belong to the country");
    }

    @Test
    void givenCountryWithStates_whenDeleteCountry_thenShouldVerifyStatesAreDeleted() {

        // Given
        int countryId = 1; // Assuming 'Afghanistan' has ID 1
        assertTrue(countryRepository.existsById(countryId), "Country with ID 1 should exist");
        List<State> statesBeforeDeletion = stateRepository.findAll();
        assertFalse(statesBeforeDeletion.isEmpty(), "States should not be empty before deletion");

        // When
        countryRepository.deleteById(countryId);

        // Then
        assertFalse(countryRepository.existsById(countryId), "Country with ID 1 should be deleted");
        List<State> statesAfterDeletion = stateRepository.findAll();
        statesAfterDeletion.forEach(state -> assertNotEquals(countryId, state.getCountry().getId(), "States of deleted country should be deleted"));
    }
}

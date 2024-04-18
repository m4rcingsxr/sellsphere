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
class StateRepositoryTest {

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private StateRepository stateRepository;

    @Test
    void givenStatesLoaded_whenFindAll_thenShouldReturnAllStates() {

        // When
        List<State> states = stateRepository.findAll();

        // Then
        assertNotNull(states, "The list of states should not be null");
        assertFalse(states.isEmpty(), "The list of states should not be empty");
        assertEquals(6, states.size(), "The number of states should be 6");
    }

    @Test
    void givenStateId1_whenFindById_thenShouldReturnStateWithId1() {

        // Given
        int id = 1;

        // When
        Optional<State> state = stateRepository.findById(id);

        // Then
        assertTrue(state.isPresent(), "State with ID 1 should be present");
        assertEquals("Kabul", state.get().getName(), "State name should be Kabul");
    }

    @Test
    void givenNewState_whenSave_thenShouldSaveNewState() {

        // Given
        Optional<Country> optionalCountry = countryRepository.findById(1); // Assuming 'Afghanistan' has ID 1
        assertTrue(optionalCountry.isPresent(), "Country with ID 1 should be present");
        Country country = optionalCountry.get();

        State state = new State();
        state.setName("New State");
        state.setCountry(country);

        // When
        State savedState = stateRepository.save(state);

        // Then
        assertNotNull(savedState, "Saved state should not be null");
        assertNotNull(savedState.getId(), "Saved state should have an ID");
        assertEquals("New State", savedState.getName(), "State name should be 'New State'");
        assertEquals(country.getId(), savedState.getCountry().getId(), "State should belong to the country");
    }

    @Test
    void givenStateId1_whenDeleteById_thenShouldDeleteStateWithId1() {

        // Given
        int id = 1; // Assuming 'Kabul' has ID 1
        assertTrue(stateRepository.existsById(id), "State with ID 1 should exist");

        // When
        stateRepository.deleteById(id);

        // Then
        assertFalse(stateRepository.existsById(id), "State with ID 1 should be deleted");
    }

    @Test
    void givenStatesLoaded_whenFindAllSortedByName_thenShouldReturnAllStatesSortedByName() {

        // When
        List<State> states = stateRepository.findAll(Sort.by("name"));

        // Then
        assertNotNull(states, "The list of states should not be null");
        assertFalse(states.isEmpty(), "The list of states should not be empty");
        assertEquals(6, states.size(), "The number of states should be 6");
        assertEquals("Algiers", states.get(0).getName(), "The first state should be 'Algiers'");
        assertEquals("Tirana", states.get(states.size() - 1).getName(), "The last state should be 'Tirana'");
    }

    @Test
    void givenStatesLoaded_whenFindAllByCountry_thenShouldReturnAllStatesSortedByName() {

        // Given - existing country
        Country country = new Country();
        country.setId(1);

        // When
        List<State> states = stateRepository.findAllByCountry(country,
                                                            Sort.by(Sort.Direction.ASC, "name")
        );

        // Then
        assertFalse(states.isEmpty());
        assertEquals(2, states.size());
    }
}

package com.sellsphere.client.setting;

import com.sellsphere.common.entity.Country;
import com.sellsphere.common.entity.State;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS, scripts = "classpath:sql/countries.sql")
class StateRepositoryTest {

    @Autowired
    private StateRepository stateRepository;

    @Autowired
    private CountryRepository countryRepository;


    @Test
    void givenPredefinedDbStates_whenListByCountry_thenReturnLinkedStates() {
        Optional<Country> country = countryRepository.findById(1);
        assertTrue(country.isPresent());

        List<State> states = stateRepository.findAllByCountry(country.get());

        assertEquals(2, states.size());
    }



}
package com.sellsphere.client.setting;

import com.sellsphere.common.entity.Country;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.jdbc.Sql;

import java.util.Comparator;
import java.util.List;

import static com.sellsphere.client.PageUtil.assertSortOrder;
import static com.sellsphere.client.PageUtil.generateSort;

@DataJpaTest
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS, scripts = {"classpath:sql/currencies.sql", "classpath:sql/countries.sql", "classpath:sql/states.sql"})
class CountryRepositoryTest {

    @Autowired
    private CountryRepository countryRepository;

    @Test
    void givenLoadedCountries_whenFindAll_thenReturnCountriesInCorrectOrder() {
        List<Country> countries = countryRepository.findAll(generateSort("name", Sort.Direction.ASC));

        assertSortOrder(countries, Comparator.comparing(Country::getName));
    }


}
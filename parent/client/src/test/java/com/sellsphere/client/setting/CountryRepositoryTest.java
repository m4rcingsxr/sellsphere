package com.sellsphere.client.setting;

import com.sellsphere.client.customer.CustomerService;
import com.sellsphere.common.entity.Constants;
import com.sellsphere.common.entity.Country;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.Comparator;
import java.util.List;

import static com.sellsphere.client.PageUtil.assertSortOrder;
import static com.sellsphere.client.PageUtil.generateSort;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DataJpaTest
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS, scripts = "classpath:sql/countries.sql")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@ExtendWith(MockitoExtension.class)
class CountryRepositoryTest {

    @Autowired
    private CountryRepository countryRepository;

    @Test
    void givenLoadedCountries_whenFindAll_thenReturnCountriesInCorrectOrder() {
        List<Country> countries = countryRepository.findAll(generateSort("name", Constants.SORT_ASCENDING));

        assertSortOrder(countries, Comparator.comparing(Country::getName));
    }


}
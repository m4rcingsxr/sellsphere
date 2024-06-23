package com.sellsphere.client.setting;

import com.sellsphere.common.entity.Currency;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Sql(scripts = "classpath:sql/currencies.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CurrencyRepositoryTest {

    @Autowired
    private CurrencyRepository repository;

    // find by id
    @Test
    void givenExistingCurrencyId_whenFindById_thenReturnCurrency() {

        // given
        Integer currencyId = 1;

        // when
        Optional<Currency> currencyOpt = repository.findById(currencyId);

        // then
        assertTrue(currencyOpt.isPresent());
        assertCurrency(currencyOpt.get());
    }

    @Test
    void givenCurrencyCode_whenFindByCode_thenReturnCurrency() {

        String currencyCode = "eur";

        Optional<Currency> currencyOpt = repository.findByCode(currencyCode);
        assertTrue(currencyOpt.isPresent());
        assertCurrency(currencyOpt.get());

    }

    private void assertCurrency(Currency currency) {
        assertAll(
                () -> assertNotNull(currency.getId()),
                () -> assertNotNull(currency.getUnitAmount()),
                () -> assertNotNull(currency.getSymbol()),
                () -> assertNotNull(currency.getCode())
        );
    }

}
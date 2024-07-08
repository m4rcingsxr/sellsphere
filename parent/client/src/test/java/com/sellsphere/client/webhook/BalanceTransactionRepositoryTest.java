package com.sellsphere.client.webhook;

import com.sellsphere.common.entity.BalanceTransaction;
import com.sellsphere.common.entity.Currency;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BalanceTransactionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BalanceTransactionRepository balanceTransactionRepository;

    @Test
    void given_whenSave_thenBalanceTransactionShouldHaveBeenPersisted() {
        // Given
        Currency currency = Currency.builder()
                .code("USD")
                .symbol("$")
                .name("US Dollar")
                .unitAmount(BigDecimal.valueOf(100L))
                .build();
        currency = entityManager.persistAndFlush(currency);

        BalanceTransaction balanceTransaction = BalanceTransaction.builder()
                .stripeId("txn_test_id")
                .amount(1000L)
                .created(System.currentTimeMillis())
                .fee(100L)
                .currency(currency)
                .net(900L)
                .exchangeRate(BigDecimal.valueOf(1.0))
                .build();

        // When
        BalanceTransaction savedTransaction = balanceTransactionRepository.save(balanceTransaction);

        // Then
        BalanceTransaction foundTransaction = entityManager.find(BalanceTransaction.class, savedTransaction.getId());
        assertThat(foundTransaction).isNotNull();
        assertThat(foundTransaction.getId()).isEqualTo(savedTransaction.getId());
        assertThat(foundTransaction.getStripeId()).isEqualTo("txn_test_id");
        assertThat(foundTransaction.getAmount()).isEqualTo(1000L);
        assertThat(foundTransaction.getCreated()).isEqualTo(balanceTransaction.getCreated());
        assertThat(foundTransaction.getFee()).isEqualTo(100L);
        assertThat(foundTransaction.getCurrency().getCode()).isEqualTo("USD");
        assertThat(foundTransaction.getNet()).isEqualTo(900L);
        assertThat(foundTransaction.getExchangeRate()).isEqualTo(BigDecimal.valueOf(1.0));
    }
}

package com.sellsphere.client.webhook;

import com.sellsphere.common.entity.Country;
import com.sellsphere.common.entity.Currency;
import com.sellsphere.common.entity.Customer;
import com.sellsphere.common.entity.PaymentMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PaymentMethodRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Test
    void whenSave_thenPaymentMethodIsPersisted() {

        // Given
        Currency currency = Currency.builder()
                .code("USD")
                .symbol("$")
                .name("US Dollar")
                .unitAmount(BigDecimal.valueOf(100L))
                .build();
        currency = entityManager.persistAndFlush(currency);

        Country country = Country.builder()
                .name("United States")
                .code("USA")
                .currency(currency)
                .build();
        country = entityManager.persistAndFlush(country);

        Customer customer = Customer.builder()
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .enabled(true)
                .emailVerified(true)
                .createdTime(LocalDateTime.now())
                .build();
        customer = entityManager.persistAndFlush(customer);

        PaymentMethod paymentMethod = PaymentMethod.builder()
                .stripeId("pm_test_id")
                .billingCountry(country)
                .customer(customer)
                .type("card")
                .build();

        // When
        PaymentMethod savedPaymentMethod = paymentMethodRepository.save(paymentMethod);

        // Then
        PaymentMethod foundPaymentMethod = entityManager.find(PaymentMethod.class, savedPaymentMethod.getId());
        assertThat(foundPaymentMethod).isNotNull();
        assertThat(foundPaymentMethod.getStripeId()).isEqualTo("pm_test_id");
        assertThat(foundPaymentMethod.getBillingCountry().getName()).isEqualTo("United States");
        assertThat(foundPaymentMethod.getCustomer().getEmail()).isEqualTo("test@example.com");
        assertThat(foundPaymentMethod.getType()).isEqualTo("card");
    }
}

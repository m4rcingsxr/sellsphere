package com.sellsphere.client.webhook;

import com.sellsphere.common.entity.Charge;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ChargeRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ChargeRepository chargeRepository;

    @Test
    void givenChargeData_whenSave_thenChargeShouldBePersisted() {
        // Given
        Charge charge = Charge.builder()
                .stripeId("test_stripe_id")
                .amount(1000L)
                .amountRefunded(0L)
                .receiptUrl("http://example.com/receipt")
                .refunded(false)
                .status("succeeded")
                .build();
        entityManager.persistAndFlush(charge);

        // When
        Optional<Charge> foundCharge = chargeRepository.findByStripeId("test_stripe_id");

        // Then
        assertThat(foundCharge).isPresent();
        assertThat(foundCharge.get().getStripeId()).isEqualTo("test_stripe_id");
        assertThat(foundCharge.get().getAmount()).isEqualTo(1000L);
        assertThat(foundCharge.get().getAmountRefunded()).isEqualTo(0L);
        assertThat(foundCharge.get().getReceiptUrl()).isEqualTo("http://example.com/receipt");
        assertThat(foundCharge.get().getRefunded()).isFalse();
        assertThat(foundCharge.get().getStatus()).isEqualTo("succeeded");
    }

    @Test
    void givenNotExistingId_whenFindByStripeId_thenShouldReturnEmptyOptional() {

        // When
        Optional<Charge> foundCharge = chargeRepository.findByStripeId("non_existent_stripe_id");

        // Then
        assertThat(foundCharge).isNotPresent();
    }
}
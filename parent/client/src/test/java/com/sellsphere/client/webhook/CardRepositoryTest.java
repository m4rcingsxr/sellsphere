package com.sellsphere.client.webhook;

import com.sellsphere.common.entity.Card;
import com.sellsphere.common.entity.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
class CardRepositoryTest {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void givenCardData_whenSave_thenShouldAssignIdAndSaveCard() {

        // Given
        Customer customer = Customer.builder()
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .enabled(true)
                .emailVerified(true)
                .createdTime(LocalDateTime.now())
                .build();

        customer = entityManager.persistAndFlush(customer);

        Card card = Card.builder()
                .customer(customer)
                .brand("Visa")
                .country("US")
                .expMonth(12L)
                .expYear(2025L)
                .funding("credit")
                .last4("1234")
                .created(System.currentTimeMillis())
                .build();

        // When
        card = cardRepository.saveAndFlush(card);

        // Then
        Optional<Card> retrievedCard = cardRepository.findById(card.getId());
        assertThat(retrievedCard).isPresent();
        assertThat(retrievedCard.get().getBrand()).isEqualTo("Visa");
        assertThat(retrievedCard.get().getCountry()).isEqualTo("US");
        assertThat(retrievedCard.get().getExpMonth()).isEqualTo(12L);
        assertThat(retrievedCard.get().getExpYear()).isEqualTo(2025L);
        assertThat(retrievedCard.get().getFunding()).isEqualTo("credit");
        assertThat(retrievedCard.get().getLast4()).isEqualTo("1234");
    }

}
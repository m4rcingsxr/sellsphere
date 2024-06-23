package com.sellsphere.client.checkout;

import com.sellsphere.common.entity.*;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;

import static com.sellsphere.client.checkout.CheckoutTestUtil.createTransaction;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS,
        scripts = "classpath:sql/checkout.sql")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PaymentIntentRepositoryTest {

    @Autowired
    private PaymentIntentRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void givenEntitiesExist_whenSavingPaymentIntent_thenShouldBeSaved() {
        // given
        PaymentIntent transaction = prepareAndCreateTransaction();

        // when
        PaymentIntent savedTransaction = repository.save(transaction);

        // then
        assertNotNull(savedTransaction.getId(), "Saved transaction should have an ID");

        // Verify that the transaction can be retrieved
        Optional<PaymentIntent> retrievedTransaction = repository.findById(savedTransaction.getId());
        assertTrue(retrievedTransaction.isPresent(), "Transaction should be retrievable from the database");
    }

    @Test
    void givenSavedPaymentIntent_whenDeleting_thenShouldBeDeleted() {
        // given
        PaymentIntent transaction = prepareAndCreateTransaction();
        PaymentIntent savedTransaction = repository.save(transaction);
        assertNotNull(savedTransaction.getId(), "Saved transaction should have an ID");

        // when
        repository.delete(savedTransaction);

        // then
        Optional<PaymentIntent> deletedTransaction = repository.findById(savedTransaction.getId());
        assertTrue(deletedTransaction.isEmpty(), "Deleted transaction should not be retrievable from the database");

        // Ensure that other entities are still present
        verifyEntitiesArePresent();
    }

    @Test
    void givenPaymentIntentExists_whenFindByStripeId_thenShouldBeFound() {
        // given
        PaymentIntent transaction = prepareAndCreateTransaction();
        transaction.setStripeId("pi_123");
        repository.save(transaction);

        // when
        Optional<PaymentIntent> foundTransaction = repository.findByStripeId("pi_123");

        // then
        assertTrue(foundTransaction.isPresent(), "PaymentIntent should be found by stripeId");
        assertNotNull(foundTransaction.get().getId(), "Found PaymentIntent should have an ID");
    }

    private PaymentIntent prepareAndCreateTransaction() {
        Currency eur = findEntity(Currency.class, 1);
        Currency pln = findEntity(Currency.class, 2);
        Customer customer = findEntity(Customer.class, 1);
        Address address = findEntity(Address.class, 1);
        Courier courier = findEntity(Courier.class, 1);

        return createTransaction(eur, pln, address, customer, courier);
    }

    private <T> T findEntity(Class<T> entityClass, Object primaryKey) {
        T entity = entityManager.find(entityClass, primaryKey);
        assertNotNull(entity, entityClass.getSimpleName() + " should be present in the database");
        return entity;
    }

    private void verifyEntitiesArePresent() {
        assertNotNull(findEntity(Currency.class, 1));
        assertNotNull(findEntity(Currency.class, 2));
        assertNotNull(findEntity(Customer.class, 1));
        assertNotNull(findEntity(Address.class, 1));
        assertNotNull(findEntity(Courier.class, 1));
    }

}
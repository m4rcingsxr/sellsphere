package com.sellsphere.client.customer;

import com.sellsphere.client.KeycloakDatabaseConfig;
import com.sellsphere.common.entity.Customer;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;

import static com.sellsphere.client.customer.CustomerTestUtil.generateDummyCustomer;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS, scripts =
        "classpath:/sql/customers.sql")
@Import(KeycloakDatabaseConfig.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void givenPredefinedDBCustomers_whenFindByEmail_thenReturnCustomer() {
        String email = "alice.smith@example.com";

        Optional<Customer> customer = customerRepository.findByEmail(email);

        assertTrue(customer.isPresent());
        assertNotNull(customer.get().getId());
    }

    @Test
    void givenNewCustomer_whenSaveCustomer_thenCustomerIsPersisted() {
        Customer customer = generateDummyCustomer();
        customer.setId(null);

        Customer savedCustomer = customerRepository.save(customer);
        assertNotNull(savedCustomer);
        assertNotNull(savedCustomer.getId());
    }


}
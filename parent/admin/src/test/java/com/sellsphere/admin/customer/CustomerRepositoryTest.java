package com.sellsphere.admin.customer;

import com.sellsphere.common.entity.Address;
import com.sellsphere.common.entity.Country;
import com.sellsphere.common.entity.Customer;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS, scripts = "classpath:sql/customers.sql")
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EntityManager entityManager;

    @ParameterizedTest
    @CsvSource({
            "Doe, john.doe@example.com, John, Doe",
            "Smith, jane.smith@example.com, Jane, Smith",
            "Jones, alice.jones@example.com, Alice, Jones",
            "Brown, bob.brown@example.com, Bob, Brown",
            "White, carol.white@example.com, Carol, White"
    })
    void givenKeyword_whenFindAll_thenReturnMatchingCustomers(String keyword, String expectedEmail,
                                                              String expectedFirstName,
                                                              String expectedLastName) {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Customer> result = customerRepository.findAll(keyword, pageable);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.getContent().stream().anyMatch(customer ->
                                                                 customer.getEmail().equals(
                                                                         expectedEmail) &&
                                                                         customer.getFirstName().equals(
                                                                                 expectedFirstName) &&
                                                                         customer.getLastName().equals(
                                                                                 expectedLastName)
        ));
    }

    @Test
    void givenExistingEmail_whenFindByEmail_thenReturnCustomer() {
        // Given
        String email = "john.doe@example.com";

        // When
        Optional<Customer> result = customerRepository.findByEmail(email);

        // Then
        assertTrue(result.isPresent());
        Customer customer = result.get();
        assertEquals(email, customer.getEmail());
        assertEquals("John", customer.getFirstName());
        assertEquals("Doe", customer.getLastName());
    }

    @Test
    void givenNonExistingEmail_whenFindByEmail_thenReturnEmpty() {
        // Given
        String email = "non.existing@example.com";

        // When
        Optional<Customer> result = customerRepository.findByEmail(email);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void givenExistingCustomer_whenUpdate_thenShouldCustomerIsUpdated() {
        Customer customer = customerRepository.findByEmail("john.doe@example.com").orElseThrow();
        customer.setFirstName("Bob");
        customer.setLastName("Smith");

        Address address = customer.getAddresses().get(0);
        assertNotNull(address);

        address.setAddressLine1("addressLine1");

        customerRepository.saveAndFlush(customer);

        Customer savedCustomer = customerRepository.findById(customer.getId()).orElseThrow();
        assertNotNull(savedCustomer);
        assertEquals("Bob", savedCustomer.getFirstName());
        assertEquals("Smith", savedCustomer.getLastName());
        assertNotNull(savedCustomer.getAddresses().get(0));
        assertEquals("addressLine1", savedCustomer.getAddresses().get(0).getAddressLine1());
    }

    @Test
    void givenExistingCustomer_whenDelete_thenCustomerLinkedAddressesAreRemovedAndCountryRemain() {
        Customer customer = customerRepository.findByEmail("john.doe@example.com").orElseThrow();
        assertNotNull(customer.getAddresses().get(0));

        List<Integer> addresses = customer.getAddresses().stream().map(Address::getId).toList();
        Integer countryId = customer.getAddresses().get(0).getCountry().getId();

        customerRepository.delete(customer);

        for (Integer address : addresses) {
            assertNull(entityManager.find(Address.class, address));
        }
        assertNotNull(entityManager.find(Country.class, countryId));


    }

    private Customer createCustomer() {
        Customer customer = new Customer();
        customer.setPassword("bcryptedPassword");
        customer.setEmail("ben.smith@example.com");
        customer.setFirstName("Ben");
        customer.setLastName("Smith");
        customer.addAddress(createAddress());

        return customer;
    }

    private Address createAddress() {
        Country country = new Country();
        country.setId(1);
        country.setName("United States");
        country.setCode("USA");

        Address address = new Address();
        address.setFirstName("John");
        address.setLastName("Doe");
        address.setPhoneNumber("123-456-7890");
        address.setAddressLine1("123 Main St");
        address.setAddressLine2("Apt 4");
        address.setCity("New York");
        address.setState("NY");
        address.setPostalCode("10001");
        address.setCountry(country);
        return address;
    }

}
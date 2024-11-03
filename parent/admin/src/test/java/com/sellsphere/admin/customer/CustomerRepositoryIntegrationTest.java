package com.sellsphere.admin.customer;

import com.sellsphere.common.entity.Address;
import com.sellsphere.common.entity.Country;
import com.sellsphere.common.entity.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.jdbc.Sql;
import util.PagingTestHelper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Sql(scripts = {"classpath:sql/customers.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class CustomerRepositoryIntegrationTest {

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private AddressRepository addressRepository;

    @Test
    void givenKeyword_whenFindingAllCustomers_thenReturnMatchingCustomers() {
        // Given
        String keyword = "john";
        Pageable pageable = PagingTestHelper.createPageRequest(0, 5, "firstName", Sort.Direction.ASC);

        // When
        Page<Customer> result = customerRepository.findAll(keyword, pageable);

        // Then
        PagingTestHelper.assertPagingResults(result, 1, 1, 1, "firstName", true);
        assertEquals("john.doe@example.com", result.getContent().get(0).getEmail(),
                     "The customer's email should match 'john.doe@example.com'.");
    }

    @Test
    void givenInvalidKeyword_whenFindingAllCustomers_thenReturnEmptyResult() {
        // Given
        String keyword = "nomatch";
        Pageable pageable = PagingTestHelper.createPageRequest(0, 5, "firstName", Sort.Direction.ASC);

        // When
        Page<Customer> result = customerRepository.findAll(keyword, pageable);

        // Then
        PagingTestHelper.assertPagingResults(result, 0, 0, 0, "firstName", true);
    }

    @Test
    void givenValidEmail_whenFindingCustomerByEmail_thenReturnCustomer() {
        // Given
        String email = "john.doe@example.com";

        // When
        Optional<Customer> result = customerRepository.findByEmail(email);

        // Then
        assertTrue(result.isPresent(), "A customer with the email 'john.doe@example.com' should be present.");
        assertEquals("John", result.get().getFirstName(), "The first name should match 'John'.");
        assertEquals("Doe", result.get().getLastName(), "The last name should match 'Doe'.");
    }

    @Test
    void givenNonExistentEmail_whenFindingCustomerByEmail_thenReturnEmptyOptional() {
        // Given
        String email = "nonexistent@example.com";

        // When
        Optional<Customer> result = customerRepository.findByEmail(email);

        // Then
        assertFalse(result.isPresent(), "No customer should be found with the email 'nonexistent@example.com'.");
    }

    @Test
    void givenValidCustomerId_whenDeletingCustomerById_thenCustomerIsDeleted() {
        // Given
        String email = "john.doe@example.com";
        Optional<Customer> customer = customerRepository.findByEmail(email);
        assertTrue(customer.isPresent(), "Customer 'john.doe@example.com' should exist.");
        Integer customerId = customer.get().getId();

        // When
        customerRepository.deleteById(customerId);

        // Then
        Optional<Customer> deletedCustomer = customerRepository.findById(customerId);
        assertFalse(deletedCustomer.isPresent(), "Customer with ID " + customerId + " should be deleted.");
    }

    @Test
    void givenValidCustomerWithAddress_whenSavingCustomer_thenAddressIsSavedSuccessfully() {
        // Given
        Country country = new Country();
        country.setId(1);
        country.setName("Poland");

        Address address = Address.builder()
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("123-456-7890")
                .addressLine1("123 Main St")
                .city("Warsaw")
                .state("Lower Silesia")
                .postalCode("01-200")
                .country(country)
                .primary(true)
                .build();

        Customer customer = Customer.builder()
                .email("new.customer@example.com")
                .firstName("New")
                .lastName("Customer")
                .enabled(true)
                .emailVerified(true)
                .createdTime(LocalDateTime.now())
                .build();
        customer.addAddress(address);

        // When
        Customer savedCustomer = customerRepository.save(customer);

        // Then
        assertNotNull(savedCustomer.getId(), "Customer ID should not be null after saving.");
        assertEquals(1, savedCustomer.getAddresses().size(), "Customer should have 1 address.");
        assertTrue(savedCustomer.getAddresses().get(0).isPrimary(), "The saved address should be the primary one.");
    }

    @Test
    void givenCustomerWithAddress_whenDeletingCustomer_thenAddressIsDeletedAsWell() {
        // Given
        String email = "john.doe@example.com";
        Optional<Customer> customer = customerRepository.findByEmail(email);
        assertTrue(customer.isPresent(), "Customer 'john.doe@example.com' should exist.");
        Integer customerId = customer.get().getId();

        List<Address> addresses = customer.get().getAddresses();

        // When
        customerRepository.deleteById(customerId);

        // Then
        Optional<Customer> deletedCustomer = customerRepository.findById(customerId);
        assertFalse(deletedCustomer.isPresent(), "Customer with ID " + customerId + " should be deleted.");

        for (Address address : addresses) {
            assertTrue(addressRepository.findById(address.getId()).isEmpty());
        }
    }

    @Test
    void givenNewCustomerWithoutAddress_whenSavingCustomer_thenCustomerIsSavedSuccessfully() {
        // Given
        Customer customer = Customer.builder()
                .email("another.customer@example.com")
                .firstName("Another")
                .lastName("Customer")
                .enabled(true)
                .createdTime(LocalDateTime.now())
                .emailVerified(true)
                .build();

        // When
        Customer savedCustomer = customerRepository.save(customer);

        // Then
        assertNotNull(savedCustomer.getId(), "Customer ID should not be null after saving.");
        assertNull(savedCustomer.getAddresses(), "Customer should have no addresses.");
    }

    @Test
    void givenInvalidCustomerId_whenDeletingCustomerById_thenNoExceptionIsThrown() {
        // Given
        Integer nonExistentCustomerId = 99999;

        // When
        customerRepository.deleteById(nonExistentCustomerId);

        // Then
        Optional<Customer> deletedCustomer = customerRepository.findById(nonExistentCustomerId);
        assertFalse(deletedCustomer.isPresent(), "Customer with ID " + nonExistentCustomerId + " should not exist.");
    }
}

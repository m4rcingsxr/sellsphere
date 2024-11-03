package com.sellsphere.admin.customer;

import com.sellsphere.common.entity.Customer;
import com.sellsphere.common.entity.CustomerNotFoundException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;
import util.PagingTestHelper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Sql(scripts = {"classpath:sql/customers.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class CustomerServiceIntegrationTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void givenValidCustomerId_whenGetCustomer_thenReturnCustomer() throws CustomerNotFoundException {
        // Given
        Integer customerId = 1;

        // When
        Customer result = customerService.get(customerId);

        // Then
        assertNotNull(result);
        assertEquals("John", result.getFirstName(), "First name should be John.");
        assertEquals("Doe", result.getLastName(), "Last name should be Doe.");
        assertEquals("john.doe@example.com", result.getEmail(), "Email should match.");
    }

    @Test
    void givenInvalidCustomerId_whenGetCustomer_thenThrowCustomerNotFoundException() {
        // Given
        Integer invalidCustomerId = 999;

        // When / Then
        assertThrows(CustomerNotFoundException.class, () -> customerService.get(invalidCustomerId));
    }

    @Test
    void whenListAllCustomers_thenReturnListOfCustomers() {
        // When
        List<Customer> customers = customerService.listAll();

        // Then
        assertNotNull(customers);
        assertEquals(1, customers.size(), "There should be one customer in the list.");
        assertEquals("john.doe@example.com", customers.get(0).getEmail(), "The customer email should be john.doe@example.com.");
    }

    @Test
    void givenNewCustomer_whenSaveCustomer_thenCustomerIsSaved() throws CustomerNotFoundException {
        // Given
        Customer newCustomer = new Customer();
        newCustomer.setEmail("new.customer@example.com");
        newCustomer.setFirstName("New");
        newCustomer.setLastName("Customer");
        newCustomer.setPassword("newpassword123");
        newCustomer.setEnabled(true);
        newCustomer.setCreatedTime(LocalDateTime.now());
        newCustomer.setEmailVerified(true);

        // When
        customerService.save(newCustomer);

        // Then
        Optional<Customer> savedCustomer = customerRepository.findByEmail("new.customer@example.com");
        assertTrue(savedCustomer.isPresent(), "Customer should be saved in the repository.");
        assertEquals("New", savedCustomer.get().getFirstName(), "First name should match.");
        assertTrue(passwordEncoder.matches("newpassword123", savedCustomer.get().getPassword()), "Password should be encoded.");
    }

    @Test
    void givenExistingCustomerWithoutNewPassword_whenSaveCustomer_thenRetainExistingPassword() throws CustomerNotFoundException {
        // Given
        Customer existingCustomer = new Customer();
        existingCustomer.setId(1);
        existingCustomer.setFirstName("Updated");
        existingCustomer.setPassword(null);

        // When
        customerService.save(existingCustomer);

        // Then
        Customer updatedCustomer = customerService.get(1);
        assertEquals("Updated", updatedCustomer.getFirstName(), "First name should be updated.");
        assertEquals("password123", updatedCustomer.getPassword(), "Password should remain the same.");
    }

    @Test
    void givenValidCustomerId_whenDeleteCustomer_thenCustomerIsDeleted() throws CustomerNotFoundException {
        // Given
        Integer customerId = 1;
        assertTrue(customerRepository.findById(customerId).isPresent(), "Customer should exist before deletion.");

        // When
        customerService.delete(customerId);

        // Then
        assertFalse(customerRepository.findById(customerId).isPresent(), "Customer should be deleted.");
    }

    @Test
    void givenValidCustomerId_whenUpdateEnabledStatus_thenCustomerStatusIsUpdated() throws CustomerNotFoundException {
        // Given
        Integer customerId = 1;
        assertTrue(customerRepository.findById(customerId).get().isEnabled(), "Customer should be enabled initially.");

        // When
        customerService.updateCustomerEnabledStatus(customerId, false);

        // Then
        assertFalse(customerRepository.findById(customerId).get().isEnabled(), "Customer should be disabled after the update.");
    }

    @Test
    void givenNonExistentEmail_whenCheckingIfEmailIsUnique_thenReturnTrue() {
        // Given
        String email = "non.existent@example.com";

        // When
        boolean isUnique = customerService.isEmailUnique(1, email);

        // Then
        assertTrue(isUnique, "The email should be unique.");
    }

    @Test
    void givenValidPageNumber_whenListingCustomers_thenReturnCustomersPage() {
        // Given
        Pageable pageable = PagingTestHelper.createPageRequest(0, 5, "firstName", Sort.Direction.ASC);

        // When
        Page<Customer> result = customerRepository.findAll(pageable);

        // Then
        assertEquals(1, result.getTotalElements(), "There should be one customer.");
    }
}

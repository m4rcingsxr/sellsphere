package com.sellsphere.admin.customer;

import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.common.entity.Customer;
import com.sellsphere.common.entity.CustomerNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceUnitTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void givenValidCustomerId_whenGetCustomer_thenReturnCustomer() throws CustomerNotFoundException {
        // Given
        Customer customer = new Customer();
        customer.setId(1);
        customer.setFirstName("John");
        given(customerRepository.findById(1)).willReturn(Optional.of(customer));

        // When
        Customer result = customerService.get(1);

        // Then
        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        then(customerRepository).should().findById(1);
    }

    @Test
    void givenInvalidCustomerId_whenGetCustomer_thenThrowCustomerNotFoundException() {
        // Given
        given(customerRepository.findById(anyInt())).willReturn(Optional.empty());

        // When/Then
        assertThrows(CustomerNotFoundException.class, () -> customerService.get(1));
        then(customerRepository).should().findById(1);
    }

    @Test
    void whenListAllCustomers_thenReturnListOfCustomers() {
        // When
        customerService.listAll();

        // Then
        then(customerRepository).should().findAll();
    }

    @Test
    void whenListingCustomersByPage_thenVerifyListEntitiesMethodCalled() {
        // Given
        int pageNum = 1;
        PagingAndSortingHelper helper = mock(PagingAndSortingHelper.class);

        // When
        customerService.listPage(pageNum, helper);

        // Then
        then(helper).should().listEntities(eq(pageNum), eq(CustomerService.CUSTOMERS_PER_PAGE), any(CustomerRepository.class));
    }

    @Test
    void givenCustomerIdAndEmail_whenCheckingIfEmailIsUnique_thenReturnTrue() {
        // Given
        Customer customer = new Customer();
        customer.setId(1);
        customer.setEmail("existing@example.com");
        given(customerRepository.findByEmail(anyString())).willReturn(Optional.of(customer));

        // When
        boolean isUnique = customerService.isEmailUnique(1, "existing@example.com");

        // Then
        assertTrue(isUnique);
        then(customerRepository).should().findByEmail("existing@example.com");
    }

    @Test
    void givenCustomerIdAndEmail_whenCheckingIfEmailIsUnique_thenReturnFalse() {
        // Given
        Customer customer = new Customer();
        customer.setId(2);  // Different ID than the one we're checking
        customer.setEmail("existing@example.com");
        given(customerRepository.findByEmail(anyString())).willReturn(Optional.of(customer));

        // When
        boolean isUnique = customerService.isEmailUnique(1, "existing@example.com");

        // Then
        assertFalse(isUnique);
        then(customerRepository).should().findByEmail("existing@example.com");
    }

    @Test
    void givenNewCustomer_whenSaving_thenPasswordShouldBeEncoded() throws CustomerNotFoundException {
        // Given
        Customer customer = new Customer();
        customer.setId(null);  // New customer
        customer.setPassword("plainPassword");

        given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");

        // When
        customerService.save(customer);

        // Then
        then(passwordEncoder).should().encode("plainPassword");
        then(customerRepository).should().save(any(Customer.class));
    }

    @Test
    void givenExistingCustomerWithoutNewPassword_whenSaving_thenRetainExistingPassword() throws CustomerNotFoundException {
        // Given
        Customer existingCustomer = new Customer();
        existingCustomer.setId(1);
        existingCustomer.setPassword("existingPassword");

        given(customerRepository.findById(anyInt())).willReturn(Optional.of(existingCustomer));

        Customer customerToUpdate = new Customer();
        customerToUpdate.setId(1);
        customerToUpdate.setPassword(null);  // No new password provided

        // When
        customerService.save(customerToUpdate);

        // Then
        then(passwordEncoder).should(never()).encode(anyString());
        then(customerRepository).should().save(any(Customer.class));
        assertEquals("existingPassword", customerToUpdate.getPassword());
    }

    @Test
    void givenValidCustomerId_whenDeleteCustomer_thenCustomerIsDeleted() throws CustomerNotFoundException {
        // Given
        Customer customer = new Customer();
        customer.setId(1);
        given(customerRepository.findById(1)).willReturn(Optional.of(customer));

        // When
        customerService.delete(1);

        // Then
        then(customerRepository).should().delete(customer);
    }

    @Test
    void givenInvalidCustomerId_whenDeleteCustomer_thenThrowCustomerNotFoundException() {
        // Given
        given(customerRepository.findById(anyInt())).willReturn(Optional.empty());

        // When/Then
        assertThrows(CustomerNotFoundException.class, () -> customerService.delete(1));
        then(customerRepository).should().findById(1);
        then(customerRepository).should(never()).delete(any(Customer.class));
    }

    @Test
    void givenValidCustomerId_whenUpdateEnabledStatus_thenCustomerStatusIsUpdated() throws CustomerNotFoundException {
        // Given
        Customer customer = new Customer();
        customer.setId(1);
        given(customerRepository.findById(1)).willReturn(Optional.of(customer));

        // When
        customerService.updateCustomerEnabledStatus(1, true);

        // Then
        assertTrue(customer.isEnabled());
        then(customerRepository).should().save(customer);
    }

    @Test
    void givenInvalidCustomerId_whenUpdateEnabledStatus_thenThrowCustomerNotFoundException() {
        // Given
        given(customerRepository.findById(anyInt())).willReturn(Optional.empty());

        // When/Then
        assertThrows(CustomerNotFoundException.class, () -> customerService.updateCustomerEnabledStatus(1, true));
        then(customerRepository).should().findById(1);
        then(customerRepository).should(never()).save(any(Customer.class));
    }
}

package com.sellsphere.admin.customer;

import com.sellsphere.common.entity.Customer;
import com.sellsphere.common.entity.CustomerNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {
    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void givenCustomerId_whenGet_thenReturnCustomer() throws CustomerNotFoundException {
        // Given
        Integer customerId = 1;
        Customer customer = new Customer();
        customer.setId(customerId);
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        // When
        Customer result = customerService.get(customerId);

        // Then
        assertNotNull(result);
        assertEquals(customerId, result.getId());
    }

    @Test
    void givenInvalidCustomerId_whenGet_thenThrowCustomerNotFoundException() {
        // Given
        Integer customerId = 1;
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CustomerNotFoundException.class, () -> customerService.get(customerId));
    }

    @Test
    void whenFindAll_thenReturnAllCustomers() {
        // Given
        List<Customer> customers = List.of(new Customer(), new Customer());
        when(customerRepository.findAll()).thenReturn(customers);

        // When
        List<Customer> result = customerService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void givenSortFieldAndSortDir_whenListAll_thenReturnSortedCustomers() {
        // Given
        String sortField = "firstName";
        String sortDir = "asc";
        List<Customer> customers = List.of(new Customer(), new Customer());
        when(customerRepository.findAll(any(Sort.class))).thenReturn(customers);

        // When
        List<Customer> result = customerService.listAll(sortField, sortDir);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(customerRepository, times(1)).findAll(any(Sort.class));
    }

    @Test
    void givenCustomerIdAndEmail_whenIsEmailUnique_thenReturnTrueIfEmailUnique() {
        // Given
        Integer customerId = 1;
        String email = "unique@example.com";
        when(customerRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        boolean result = customerService.isEmailUnique(customerId, email);

        // Then
        assertTrue(result);
    }

    @Test
    void givenCustomerIdAndEmail_whenIsEmailUnique_thenReturnFalseIfEmailNotUnique() {
        // Given
        Integer customerId = 1;
        String email = "not.unique@example.com";
        Customer existingCustomer = new Customer();
        existingCustomer.setId(2); // Different ID to indicate non-uniqueness
        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(existingCustomer));

        // When
        boolean result = customerService.isEmailUnique(customerId, email);

        // Then
        assertFalse(result);
    }

    @Test
    void givenCustomer_whenSave_thenEncodePasswordIfNew() throws CustomerNotFoundException {
        // Given
        Customer customer = new Customer();
        customer.setPassword("newpassword");
        when(passwordEncoder.encode(anyString())).thenReturn("encodedpassword");

        // When
        customerService.save(customer);

        // Then
        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(customerCaptor.capture());
        assertEquals("encodedpassword", customerCaptor.getValue().getPassword());
    }

    @Test
    void givenCustomer_whenSave_thenRetainPasswordIfNotNew() throws CustomerNotFoundException {
        // Given
        Customer existingCustomer = new Customer();
        existingCustomer.setId(1);
        existingCustomer.setPassword("existingpassword");
        when(customerRepository.findById(existingCustomer.getId())).thenReturn(Optional.of(existingCustomer));

        Customer updatedCustomer = new Customer();
        updatedCustomer.setId(1);
        updatedCustomer.setPassword(null);

        // When
        customerService.save(updatedCustomer);

        // Then
        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(customerCaptor.capture());
        assertEquals("existingpassword", customerCaptor.getValue().getPassword());
    }

    @Test
    void givenCustomerId_whenDelete_thenCustomerDeleted() throws CustomerNotFoundException {
        // Given
        Integer customerId = 1;
        Customer customer = new Customer();
        customer.setId(customerId);
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        // When
        customerService.delete(customerId);

        // Then
        verify(customerRepository, times(1)).delete(customer);
    }

    @Test
    void givenInvalidCustomerId_whenDelete_thenThrowCustomerNotFoundException() {
        // Given
        Integer customerId = 1;
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CustomerNotFoundException.class, () -> customerService.delete(customerId));
    }

    @Test
    void givenCustomerIdAndEnabledStatus_whenUpdateCustomerEnabledStatus_thenCustomerStatusUpdated() throws CustomerNotFoundException {
        // Given
        Integer customerId = 1;
        boolean enabled = true;
        Customer customer = new Customer();
        customer.setId(customerId);
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        // When
        customerService.updateCustomerEnabledStatus(customerId, enabled);

        // Then
        verify(customerRepository, times(1)).save(customer);
        assertTrue(customer.isEnabled());
    }
}
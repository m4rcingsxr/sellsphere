package com.sellsphere.client.customer;

import com.sellsphere.common.entity.Customer;
import com.sellsphere.common.entity.CustomerNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.sellsphere.client.customer.CustomerTestUtil.generateDummyCustomer;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void givenExistingCustomer_whenGetCustomer_thenReturnCustomer()
            throws CustomerNotFoundException {
        String existingEmail = "existing@example.com";
        Customer customer = generateDummyCustomer();

        when(customerRepository.findByEmail(existingEmail)).thenReturn(Optional.of(customer));

        Customer customerByEmail = customerService.getByEmail(existingEmail);
        assertNotNull(customerByEmail);
    }

    @Test
    void givenNotExistingCustomer_whenGetCustomer_thenThrowCustomerNotFoundException() {
        String notExistingEmail = "notexisting@example.com";

        when(customerRepository.findByEmail(notExistingEmail)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> customerService.getByEmail(notExistingEmail));
    }

    @Test
    void givenExistingCustomer_whenUpdateCustomer_thenOldWillBeSetPasswordAndSaveMethodIsInvoked()
            throws CustomerNotFoundException {
        Customer customer = generateDummyCustomer();
        Customer newCustomer = generateDummyCustomer();
        newCustomer.setPassword(null);

        when(customerRepository.findByEmail(newCustomer.getEmail())).thenReturn(Optional.of(customer));
        when(customerRepository.save(newCustomer)).thenReturn(newCustomer);

        customerService.update(newCustomer);

        assertNotNull(newCustomer.getPassword());

        verify(customerRepository, times(1)).findByEmail(newCustomer.getEmail());
        verify(customerRepository, times(1)).save(newCustomer);
    }

    @Test
    void givenNotExistingCustomer_whenUpdateCustomer_thenThrowCustomerNotFoundException() {
        Customer newCustomer = generateDummyCustomer();

        when(customerRepository.findByEmail(newCustomer.getEmail())).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> customerService.update(newCustomer));

        verify(customerRepository, times(1)).findByEmail(newCustomer.getEmail());
        verifyNoMoreInteractions(customerRepository);
    }

}
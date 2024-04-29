package com.sellsphere.client.address;

import com.sellsphere.common.entity.Address;
import com.sellsphere.common.entity.AddressNotFoundException;
import com.sellsphere.common.entity.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private AddressService addressService;

    private Customer customer;
    private Address primaryAddress;
    private Address secondaryAddress;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1);

        primaryAddress = new Address();
        primaryAddress.setId(1);
        primaryAddress.setCustomer(customer);
        primaryAddress.setPrimary(true);

        secondaryAddress = new Address();
        secondaryAddress.setId(2);
        secondaryAddress.setCustomer(customer);
        secondaryAddress.setPrimary(false);
    }

    @Test
    void givenNonPrimaryAddress_whenDeleted_thenNoNewPrimarySet() throws AddressNotFoundException {
        // Given
        when(addressRepository.findById(secondaryAddress.getId())).thenReturn(Optional.of(secondaryAddress));

        // When
        addressService.delete(secondaryAddress.getId());

        // Then
        verify(addressRepository).delete(secondaryAddress);
        verify(addressRepository, never()).save(any(Address.class));
    }

    @Test
    void givenPrimaryAddress_whenDeleted_thenNewPrimarySet() throws AddressNotFoundException {
        // Given
        when(addressRepository.findById(primaryAddress.getId())).thenReturn(Optional.of(primaryAddress));
        when(addressRepository.findByCustomer(customer)).thenReturn(Collections.singletonList(secondaryAddress));

        // When
        addressService.delete(primaryAddress.getId());

        // Then
        verify(addressRepository).delete(primaryAddress);
        assertThat(secondaryAddress.isPrimary()).isTrue();
        verify(addressRepository).save(secondaryAddress);
    }

    @Test
    void givenPrimaryAddress_whenDeletedAndNoAddressesLeft_thenNoNewPrimarySet() throws AddressNotFoundException {
        // Given
        when(addressRepository.findById(primaryAddress.getId())).thenReturn(Optional.of(primaryAddress));
        when(addressRepository.findByCustomer(customer)).thenReturn(Collections.emptyList());

        // When
        addressService.delete(primaryAddress.getId());

        // Then
        verify(addressRepository).delete(primaryAddress);
        verify(addressRepository, never()).save(any(Address.class));
    }

    @Test
    void givenNonExistentAddress_whenDeleted_thenThrowException() {
        // Given
        when(addressRepository.findById(anyInt())).thenReturn(Optional.empty());

        // When/Then
        assertThrows(AddressNotFoundException.class, () -> addressService.delete(999));
        verify(addressRepository, never()).delete(any(Address.class));
    }
}
package com.sellsphere.client.address;

import com.sellsphere.common.entity.Address;
import com.sellsphere.common.entity.AddressNotFoundException;
import com.sellsphere.common.entity.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;

    /**
     * Retrieves an address by its ID.
     *
     * @param addressId The ID of the address to retrieve.
     * @return The retrieved address.
     * @throws AddressNotFoundException If the address with the given ID is not found.
     */
    public Address getById(Integer addressId) throws AddressNotFoundException {
        return addressRepository.findById(addressId).orElseThrow(AddressNotFoundException::new);
    }


    /**
     * Deletes an address by its ID. If the deleted address was primary,
     * sets a new default address for the customer.
     *
     * @param addressId The ID of the address to delete.
     * @throws AddressNotFoundException If the address with the given ID is not found.
     */
    public void delete(Integer addressId) throws AddressNotFoundException {
        Address address = getById(addressId);
        Customer customer = address.getCustomer();

        customer.removeAddress(address);
        addressRepository.delete(address);

        if (Boolean.TRUE.equals(address.isPrimary())) {
            setNewDefaultAddress(customer);
        }
    }

    /**
     * Saves an address. If it's the only address for a customer or if it's set to primary,
     * updates its primary status accordingly.
     *
     * @param address The address to save.
     * @return The saved address.
     */
    public Address save(Address address) {
        boolean isFirstAddress = address.getCustomer().getAddresses().isEmpty();
        boolean isSettingPrimary = address.isPrimary();

        if(isFirstAddress) {
            address.setPrimary(true);
            return addressRepository.save(address);
        }

        if(isSettingPrimary) {
            resetCurrentPrimaryAddress(address.getCustomer());
        }

        return addressRepository.save(address);
    }

    /**
     * Sets a new default address for a customer.
     *
     * @param customer The customer whose default address to reset.
     */
    private void setNewDefaultAddress(Customer customer) {
        List<Address> addresses = addressRepository.findByCustomer(customer);

        if (!addresses.isEmpty()) {
            Address newDefaultAddress = addresses.get(0);
            newDefaultAddress.setPrimary(true);
            addressRepository.save(newDefaultAddress);
        }
    }

    /**
     * Resets the primary status of all addresses for a customer.
     *
     * @param customer The customer whose addresses to reset.
     */
    private void resetCurrentPrimaryAddress(Customer customer) {
        Address defaultAddress = addressRepository.findByPrimaryIsTrueAndCustomer(customer);

        if (defaultAddress != null) {
            defaultAddress.setPrimary(false);
            addressRepository.save(defaultAddress);
        }
    }

}

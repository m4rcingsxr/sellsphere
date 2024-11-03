package com.sellsphere.client.address;

import com.sellsphere.client.customer.CustomerRepository;
import com.sellsphere.client.setting.CountryRepository;
import com.sellsphere.common.entity.Address;
import com.sellsphere.common.entity.Country;
import com.sellsphere.common.entity.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;

import static com.sellsphere.client.address.AddressTestUtil.generateDummyAddress1;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS, scripts = {
        "classpath:sql/customers.sql", "classpath:sql/currencies.sql", "classpath:sql/countries.sql"} )
class AddressRepositoryTest {

    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private CountryRepository countryRepository;

    @Test
    void givenNewAddress_whenSaveAddress_thenAddressIsPersistedWithAssignedId() {
        Address address = generateDummyAddress1();

        Optional<Country> country = countryRepository.findById(1);
        Optional<Customer> customer = customerRepository.findById(1);
        assertTrue(customer.isPresent());
        assertTrue(country.isPresent());

        address.setCustomer(customer.get());
        address.setCountry(country.get());
        address.setId(null);

        Address savedAddress = addressRepository.save(address);

        assertNotNull(savedAddress.getId());
    }

    @Test
    void givenExistingAddress_whenDelete_thenAddressIsRemoved() {
        givenNewAddress_whenSaveAddress_thenAddressIsPersistedWithAssignedId();

        assertTrue(addressRepository.existsById(1));
        addressRepository.deleteById(1);

        assertFalse(addressRepository.existsById(1));
    }

}
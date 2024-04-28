package com.sellsphere.client.customer;

import com.sellsphere.client.address.AddressRepository;
import com.sellsphere.client.setting.CountryRepository;
import com.sellsphere.common.entity.Address;
import com.sellsphere.common.entity.Country;
import com.sellsphere.common.entity.Customer;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static com.sellsphere.client.address.AddressTestUtil.generateDummyAddress1;
import static com.sellsphere.client.address.AddressTestUtil.generateDummyAddress2;
import static com.sellsphere.client.customer.CustomerTestUtil.generateDummyCustomer;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS, scripts =
        "classpath:/sql/customers" + ".sql")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private AddressRepository addressRepository;

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

    @Test
    void givenCustomerWithNewAddresses_whenUpdateCustomer_thenCustomerSaved() {
        Customer customer = generateDummyCustomer();
        customer.setId(null);

        Optional<Country> country = countryRepository.findById(1);
        assertTrue(country.isPresent());

        Address address1 = generateDummyAddress1();
        Address address2 = generateDummyAddress2();
        address1.setCountry(country.get());
        address2.setCountry(country.get());
        address1.setId(null);
        address2.setId(null);

        customer.addAddress(address1);
        customer.addAddress(address2);

        Customer savedCustomer = customerRepository.save(customer);

        assertNotNull(savedCustomer);
        assertNotNull(savedCustomer.getId());
        assertEquals(2, savedCustomer.getAddresses().size());
    }

    @Test
    void givenAddressesLinkedToCustomer_whenDeleteCustomer_thenAddressesAreRemoved() {
        givenCustomerWithNewAddresses_whenUpdateCustomer_thenCustomerSaved();

        Optional<Customer> byEmail = customerRepository.findByEmail("example@gmail.com");
        assertTrue(byEmail.isPresent());

        List<Address> addresses = byEmail.get().getAddresses();
        List<Integer> addressIds = addresses.stream().map(Address::getId).toList();

        customerRepository.delete(byEmail.get());

        addressIds.forEach(id -> {
            assertFalse(addressRepository.existsById(id));
        });
    }

}
package com.sellsphere.client.address;

import com.sellsphere.client.customer.CustomerService;
import com.sellsphere.client.setting.CountryRepository;
import com.sellsphere.common.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.util.List;

import static com.sellsphere.client.address.AddressTestUtil.generateDummyAddress1;
import static com.sellsphere.client.customer.CustomerTestUtil.generateDummyCustomer;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@ExtendWith(MockitoExtension.class)
class AddressControllerTest {

    private static final String BASE_URL = "/address_book";

    @MockBean
    private CustomerService customerService;

    @MockBean
    private CountryRepository countryRepository;

    @MockBean
    private AddressService addressService;

    @Autowired
    private MockMvc mockMvc;

    private Customer dummyCustomer;

    @BeforeEach
    void setUp() {
        dummyCustomer = generateDummyCustomer();
        Address primaryAddress = generateDummyAddress1();
        primaryAddress.setPrimary(true);
        primaryAddress.setCustomer(dummyCustomer);
    }

    @Test
    @WithMockUser(username = "example@gmail.com")
    void whenDisplayAddressBook_thenModelShouldContainCorrectAttributes() throws Exception {
        List<Country> countries = List.of(AddressTestUtil.generateDummyCountry());

        when(customerService.getByEmail(dummyCustomer.getEmail())).thenReturn(dummyCustomer);
        when(countryRepository.findAll(Sort.by("name").ascending())).thenReturn(countries);

        Principal principal = dummyCustomer::getEmail;

        mockMvc.perform(get(BASE_URL).principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("address/addresses"))
                .andExpect(model().attributeExists("customer"))
                .andExpect(model().attributeExists("countryList"))
                .andExpect(model().attributeExists("address"))
                .andExpect(model().attribute("customer", dummyCustomer))
                .andExpect(model().attribute("countryList", countries));

        verify(customerService, times(1)).getByEmail(dummyCustomer.getEmail());
        verify(countryRepository, times(1)).findAll(Sort.by("name").ascending());
    }

    @Test
    @WithMockUser(username = "example@gmail.com")
    void whenGivenCustomerWithUpdatedAddresses_whenCustomerIsUpdated_thenRedirectWithUpdateCall() throws Exception {
        Customer newCustomer = generateDummyCustomer();
        when(customerService.update(any(Customer.class))).thenReturn(newCustomer);

        mockMvc.perform(post(BASE_URL + "/update"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute(Constants.SUCCESS_MESSAGE, equalTo("Successfully updated addresses")))
                .andExpect(view().name("redirect:/address_book"));

        verify(customerService, times(1)).update(any(Customer.class));
    }

    @Test
    @WithMockUser(username = "example@gmail.com")
    void givenPrimaryAddress_whenDeleteAddress_thenNewPrimarySetAndRedirect() throws Exception {
        // Given
        doNothing().when(addressService).delete(2);

        // When/Then
        mockMvc.perform(get(BASE_URL + "/delete/{id}", 2))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(AddressController.ADDRESS_BOOK_DEFAULT_REDIRECT_URL));

        verify(addressService, times(1)).delete(2);
    }

    @Test
    @WithMockUser(username = "example@gmail.com")
    void givenNonExistentAddress_whenDeleteAddress_thenThrowException() throws Exception {
        // Given
        doThrow(new AddressNotFoundException()).when(addressService).delete(anyInt());

        // When/Then
        mockMvc.perform(get(BASE_URL + "/delete/999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists(Constants.ERROR_MESSAGE))
                .andExpect(view().name(AddressController.ADDRESS_BOOK_DEFAULT_REDIRECT_URL));

        verify(addressService, times(1)).delete(999);
    }
}

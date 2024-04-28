package com.sellsphere.client.address;

import com.sellsphere.client.customer.CustomerService;
import com.sellsphere.client.customer.CustomerTestUtil;
import com.sellsphere.client.setting.CountryRepository;
import com.sellsphere.common.entity.Address;
import com.sellsphere.common.entity.Constants;
import com.sellsphere.common.entity.Country;
import com.sellsphere.common.entity.Customer;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

import static com.sellsphere.client.customer.CustomerTestUtil.generateDummyCustomer;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class AddressControllerTest {

    private static final String BASE_URL = "/address_book";

    @MockBean
    private CustomerService customerService;

    @MockBean
    private CountryRepository countryRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "example@gmail.com")
    void whenDisplayAddressBook_thenModelShouldContainCorrectAttributes() throws Exception {
        Customer customer = generateDummyCustomer();
        List<Country> countries = List.of(AddressTestUtil.generateDummyCountry());

        when(customerService.getByEmail(customer.getEmail())).thenReturn(customer);
        when(countryRepository.findAll(Sort.by("name").ascending())).thenReturn(countries);

        Principal principal = customer::getEmail;

        mockMvc.perform(get(BASE_URL).principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("/address/addresses"))
                .andExpect(model().attributeExists("customer"))
                .andExpect(model().attributeExists("countryList"))
                .andExpect(model().attributeExists("address"))
                .andExpect(model().attribute("customer", customer))
                .andExpect(model().attribute("countryList", countries));

        verify(customerService, times(1)).getByEmail(customer.getEmail());
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



}
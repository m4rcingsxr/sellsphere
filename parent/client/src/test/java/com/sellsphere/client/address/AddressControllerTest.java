package com.sellsphere.client.address;

import com.sellsphere.client.customer.CustomerService;
import com.sellsphere.client.customer.CustomerTestUtil;
import com.sellsphere.client.setting.CountryRepository;
import com.sellsphere.common.entity.Address;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class AddressControllerTest {

    @MockBean
    private CustomerService customerService;

    @MockBean
    private CountryRepository countryRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "example@gmail.com")
    void whenDisplayAddressBook_thenModelShouldContainCorrectAttributes() throws Exception {
        Customer customer = CustomerTestUtil.generateDummyCustomer();
        List<Country> countries = List.of(AddressTestUtil.generateDummyCountry());

        when(customerService.getByEmail(customer.getEmail())).thenReturn(customer);
        when(countryRepository.findAll(Sort.by("name").ascending())).thenReturn(countries);

        Principal principal = customer::getEmail;

        mockMvc.perform(get("/address_book").principal(principal))
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

}
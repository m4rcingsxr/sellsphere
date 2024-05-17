package com.sellsphere.admin.customer;


import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.admin.setting.CountryRepository;
import com.sellsphere.common.entity.Constants;
import com.sellsphere.common.entity.Country;
import com.sellsphere.common.entity.Customer;
import com.sellsphere.common.entity.CustomerNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private CountryRepository countryRepository;

    @Test
    void givenNoParameters_whenListFirstPage_thenRedirectToDefaultSortedPage() throws Exception {
        mockMvc.perform(get("/customers"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(CustomerController.DEFAULT_REDIRECT_URL.replace("redirect:" , "")));
    }

    @Test
    void givenPageNumAndSortParameters_whenListPage_thenReturnSortedCustomerList() throws Exception {
        // Given
        Customer customer1 = new Customer();
        customer1.setId(1);
        customer1.setFirstName("John");
        customer1.setLastName("Doe");
        customer1.setEmail("john.doe@example.com");

        Customer customer2 = new Customer();
        customer2.setId(2);
        customer2.setFirstName("Jane");
        customer2.setLastName("Smith");
        customer2.setEmail("jane.smith@example.com");

        doAnswer(invocation -> {
            PagingAndSortingHelper helper = invocation.getArgument(1);
            helper.updateModelAttributes(0, 1, 2, Arrays.asList(customer1, customer2));
            return null;
        }).when(customerService).listPage(anyInt(), any(PagingAndSortingHelper.class));

        // When & Then
        mockMvc.perform(get("/customers/page/1")
                                .param("sortField", "firstName")
                                .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/customers"))
                .andExpect(model().attributeExists("customerList"))
                .andExpect(model().attributeExists("currentPage"))
                .andExpect(model().attributeExists("totalPages"))
                .andExpect(model().attributeExists("totalItems"))
                .andExpect(model().attributeExists("sortField"))
                .andExpect(model().attributeExists("sortDir"))
                .andExpect(model().attributeExists("reversedSortDir"))
                .andExpect(model().attribute("sortField", "firstName"))
                .andExpect(model().attribute("sortDir", "asc"))
                .andExpect(model().attribute("reversedSortDir", "desc"))
                .andExpect(model().attribute("customerList", Arrays.asList(customer1, customer2)));

        verify(customerService, times(1)).listPage(anyInt(), any(PagingAndSortingHelper.class));
    }

    @Test
    void givenCustomerId_whenShowCustomerForm_thenReturnCustomerFormView() throws Exception {
        // Given
        Integer customerId = 1;
        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("john.doe@example.com");

        Country country1 = new Country();
        country1.setId(1);
        country1.setName("United States");

        Country country2 = new Country();
        country2.setId(2);
        country2.setName("Canada");

        List<Country> countryList = Arrays.asList(country1, country2);

        when(customerService.get(customerId)).thenReturn(customer);
        when(countryRepository.findAllByOrderByName()).thenReturn(countryList);

        // When & Then
        mockMvc.perform(get("/customers/edit/{id}", customerId))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/customer_form"))
                .andExpect(model().attributeExists("countryList"))
                .andExpect(model().attributeExists("customer"))
                .andExpect(model().attribute("countryList", countryList))
                .andExpect(model().attribute("customer", customer));

        verify(customerService, times(1)).get(customerId);
        verify(countryRepository, times(1)).findAllByOrderByName();
    }

    @Test
    void givenCustomerIdAndStatusEnabled_whenUpdateCustomerEnabledStatus_thenRedirectToCustomerListWithSuccessMessage() throws Exception {
        Integer customerId = 1;
        boolean enabled = true;

        doNothing().when(customerService).updateCustomerEnabledStatus(customerId, enabled);

        mockMvc.perform(get("/customers/{id}/enabled/{status}", customerId, enabled)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists(Constants.SUCCESS_MESSAGE))
                .andExpect(redirectedUrl(CustomerController.DEFAULT_REDIRECT_URL.replace("redirect:" , "")));
    }

    @Test
    void givenCustomerIdAndStatusDisabled_whenUpdateCustomerEnabledStatus_thenRedirectToCustomerListWithSuccessMessage() throws Exception {
        Integer customerId = 1;
        boolean enabled = false;

        doNothing().when(customerService).updateCustomerEnabledStatus(customerId, enabled);

        mockMvc.perform(get("/customers/{id}/enabled/{status}", customerId, enabled)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(CustomerController.DEFAULT_REDIRECT_URL.replace("redirect:" , "")));
    }

    @Test
    void givenNonExistingCustomerId_whenUpdateCustomerEnabledStatus_thenThrowCustomerNotFoundException() throws Exception {
        Integer customerId = 999;
        boolean enabled = true;

        doThrow(new CustomerNotFoundException("Customer not found")).when(customerService).updateCustomerEnabledStatus(customerId, enabled);

        // Performing the GET request and verifying the response
        mockMvc.perform(get("/customers/{id}/enabled/{status}", customerId, enabled)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(CustomerController.DEFAULT_REDIRECT_URL.replace("redirect:" , "")));;
    }

    @Test
    void givenCustomerId_whenCustomerDetails_thenReturnCustomerDetailsView() throws Exception {
        Integer customerId = 1;
        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setFirstName("John Doe");
        customer.setEmail("john.doe@example.com");

        when(customerService.get(customerId)).thenReturn(customer);

        mockMvc.perform(get("/customers/details/{id}", customerId)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("customer"))
                .andExpect(model().attribute("customer", customer))
                .andExpect(view().name("customer/customer_details"));
    }

    @Test
    void givenNonExistingCustomerId_whenCustomerDetails_thenThrowCustomerNotFoundException() throws Exception {
        Integer customerId = 999;

        doThrow(new CustomerNotFoundException("Customer not found")).when(customerService).get(customerId);

        mockMvc.perform(get("/customers/details/{id}", customerId)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(flash().attributeExists(Constants.ERROR_MESSAGE))
                .andExpect(status().is3xxRedirection());
    }
}

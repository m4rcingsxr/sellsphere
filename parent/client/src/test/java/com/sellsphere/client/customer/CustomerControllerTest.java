package com.sellsphere.client.customer;

import com.sellsphere.common.entity.Constants;
import com.sellsphere.common.entity.Customer;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static com.sellsphere.client.customer.CustomerTestUtil.generateDummyCustomer;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CustomerControllerTest {

    private static final String BASE_URL = "/customer";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    @Test
    @WithMockUser(username = "example@gmail.com")
    void givenExistingCustomerEmail_whenDisplayDetails_thenModelIsLoadedWithCorrectAttributes()
            throws Exception {
        Customer customer = generateDummyCustomer();

        when(customerService.getByEmail(customer.getEmail())).thenReturn(customer);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(CustomerController.CUSTOMER_FORM_URL))
                .andExpect(model().attributeExists("customer"));

        verify(customerService, times(1)).getByEmail(customer.getEmail());
    }

    @Test
    @WithMockUser(username = "example@gmail.com")
    void givenValidCustomerUpdate_whenUpdateCustomer_thenCustomerIsUpdated() throws Exception {
        Customer existingCustomer = generateDummyCustomer();
        Customer updatedCustomer = generateDummyCustomer();
        updatedCustomer.setFirstName("updatedFirstName");
        updatedCustomer.setLastName("updatedLastName");

        when(customerService.update(any(Customer.class))).thenReturn(updatedCustomer);
        when(customerService.getByEmail(existingCustomer.getEmail())).thenReturn(existingCustomer);

        mockMvc.perform(post(BASE_URL + "/update")
                                .param("email", existingCustomer.getEmail())
                                .param("firstName", "updatedFirstName")
                                .param("lastName", "updatedLastName")
                                .param("enabled", String.valueOf(existingCustomer.isEnabled()))
                                .param("emailVerified",
                                       String.valueOf(existingCustomer.isEmailVerified())
                                )
                                .param("createdTime", existingCustomer.getCreatedTime().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:" + CustomerController.CUSTOMER_FORM_URL))
                .andExpect(flash().attributeExists(Constants.SUCCESS_MESSAGE));

        verify(customerService, times(1)).update(any(Customer.class));
    }

}
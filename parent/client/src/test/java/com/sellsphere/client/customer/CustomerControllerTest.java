package com.sellsphere.client.customer;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CustomerControllerTest {

    private static final String BASE_URL = "/customer";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    @Test
    @WithMockUser(username = "existing@example.com")
    void givenExistingCustomerEmail_whenDisplayDetails_thenModelIsLoadedWithCorrectAttributes() throws Exception {
        String existingEmail = "existing@example.com";
        Customer customer = generateDummyCustomer();
        customer.setEmail(existingEmail);

        when(customerService.getByEmail(existingEmail)).thenReturn(customer);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(CustomerController.CUSTOMER_FORM_URL))
                .andExpect(model().attributeExists("customer"));

        verify(customerService, times(1)).getByEmail(existingEmail);
    }



}
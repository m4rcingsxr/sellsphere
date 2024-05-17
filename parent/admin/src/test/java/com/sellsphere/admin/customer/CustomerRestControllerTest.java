package com.sellsphere.admin.customer;

import com.sellsphere.common.entity.CustomerNotFoundException;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@SpringBootTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CustomerRestControllerTest {

    @MockBean
    private CustomerService customerService;

    @Autowired
    private MockMvc mvc;

    @Test
    void givenEmailRequestParam_whenCheckUniqueness_thenShouldReturnTrue() throws Exception {
        Integer customerId = 1;
        String email = "test@example.com";

        when(customerService.isEmailUnique(customerId, email)).thenReturn(true);

        mvc.perform(post("/customers/check_uniqueness")
                                .param("email", email)
                                .param("id", customerId.toString())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    private static final String DEFAULT_REDIRECT_URL = "/customers/page/0?sortField=firstName&sortDir=asc";
    private static final String SUCCESS_MESSAGE = "Customer successfully removed.";

    @Test
    void givenCustomerId_whenDeleteCustomer_thenRedirectToCustomerListWithSuccessMessage() throws Exception {
        Integer customerId = 1;

        doNothing().when(customerService).delete(customerId);

        mvc.perform(get("/customers/delete/{id}", customerId)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("successMessage", SUCCESS_MESSAGE))
                .andExpect(redirectedUrl(DEFAULT_REDIRECT_URL));
    }

    @Test
    void givenNonExistingCustomerId_whenDeleteCustomer_thenThrowCustomerNotFoundException() throws Exception {
        Integer customerId = 999;

        doThrow(new CustomerNotFoundException("Customer not found")).when(customerService).delete(customerId);

        mvc.perform(get("/customers/delete/{id}", customerId)
                            .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(DEFAULT_REDIRECT_URL));
    }



}
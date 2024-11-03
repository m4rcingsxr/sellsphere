package com.sellsphere.admin.customer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class CustomerRestControllerTest {

    @MockBean
    private CustomerService customerService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void givenValidCustomerEmailWithoutId_whenCheckEmailUniqueness_thenReturnTrue() throws Exception {
        // Given a valid email without an id (new customer scenario)
        given(customerService.isEmailUnique(null, "unique.email@example.com")).willReturn(true);

        // When & Then
        mockMvc.perform(post("/customers/check-uniqueness")
                                .param("email", "unique.email@example.com")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        then(customerService).should().isEmailUnique(null, "unique.email@example.com");
    }

    @Test
    void givenValidCustomerEmailWithId_whenCheckEmailUniqueness_thenReturnTrue() throws Exception {
        // Given a valid email with an id (existing customer scenario)
        given(customerService.isEmailUnique(1, "unique.email@example.com")).willReturn(true);

        // When & Then
        mockMvc.perform(post("/customers/check-uniqueness")
                                .param("email", "unique.email@example.com")
                                .param("id", "1")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        then(customerService).should().isEmailUnique(1, "unique.email@example.com");
    }

    @Test
    void givenNonUniqueCustomerEmail_whenCheckEmailUniqueness_thenReturnFalse() throws Exception {
        // Given a non-unique email
        given(customerService.isEmailUnique(1, "existing.email@example.com")).willReturn(false);

        // When & Then
        mockMvc.perform(post("/customers/check-uniqueness")
                                .param("email", "existing.email@example.com")
                                .param("id", "1")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));

        then(customerService).should().isEmailUnique(1, "existing.email@example.com");
    }

}

package com.sellsphere.admin.customer;


import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.common.entity.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

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
}
package com.sellsphere.admin.customer;

import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.admin.setting.CountryRepository;
import com.sellsphere.common.entity.Country;
import com.sellsphere.common.entity.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Mock
    private BindingResult bindingResult;

    @Mock
    private RedirectAttributes redirectAttributes;

    private List<Country> countries;
    private Customer customer;

    @BeforeEach
    void setUp() {
        Country country1 = Country.builder().name("United States").code("US").build();
        Country country2 = Country.builder().name("Canada").code("CA").build();
        country1.setId(1);
        country2.setId(2);
        countries = Arrays.asList(
            country1, country2
        );

        customer = Customer.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .enabled(true)
                .emailVerified(true)
                .build();
        customer.setId(1);
    }

    @Test
    void given_requestToCustomers_when_redirectToFirstPage_then_shouldRedirectToFirstPage() throws Exception {
        mockMvc.perform(get("/customers"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customers/page/0?sortField=firstName&sortDir=asc"));
    }

    @Test
    void given_validPageNumAndHelper_when_listCustomersByPage_then_shouldReturnCustomerPageView() throws Exception {
        String sortField = "name";
        String sortDir = "asc";
        String keyword = "testKeyword";

        mockMvc.perform(get("/customers/page/1")
                                .param("sortField", sortField)
                                .param("sortDir", sortDir)
                                .param("keyword", keyword))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/customers"));

        then(customerService).should().listPage(eq(1), any(PagingAndSortingHelper.class));
    }


    @Test
    void given_validCustomerId_when_showCustomerForm_then_shouldReturnCustomerFormViewWithCountries() throws Exception {
        given(customerService.get(1)).willReturn(customer);
        given(countryRepository.findAllByOrderByName()).willReturn(countries);

        mockMvc.perform(get("/customers/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/customer_form"))
                .andExpect(model().attributeExists("customer"))
                .andExpect(model().attributeExists("countryList"));

        then(customerService).should().get(1);
        then(countryRepository).should().findAllByOrderByName();
    }

    @Test
    void given_validCustomer_when_updateCustomer_then_shouldRedirectToCustomerList() throws Exception {
        given(bindingResult.hasErrors()).willReturn(false);

        mockMvc.perform(post("/customers/update")
                                .param("id", "1")
                                .param("email", "john.doe@example.com")
                                .param("firstName", "John")
                                .param("lastName", "Doe"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customers/page/0?sortField=firstName&sortDir=asc&keyword=john.doe"));

        then(customerService).should().save(any(Customer.class));
    }

    @Test
    void given_invalidCustomerData_when_updateCustomer_then_shouldReturnToCustomerFormWithErrors() throws Exception {
        given(bindingResult.hasErrors()).willReturn(true);
        given(countryRepository.findAllByOrderByName()).willReturn(countries);

        mockMvc.perform(post("/customers/update")
                                .param("id", "1")
                                .param("email", "invalid-email"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/customer_form"))
                .andExpect(model().attributeExists("countryList"));

        then(customerService).should(never()).save(any(Customer.class));
    }

    @Test
    void given_validCustomerId_when_deleteCustomer_then_shouldRedirectToCustomerList() throws Exception {
        mockMvc.perform(get("/customers/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customers/page/0?sortField=firstName&sortDir=asc"));

        then(customerService).should().delete(1);
    }

    @Test
    void given_validCustomerIdAndEnabledStatus_when_updateCustomerEnabledStatus_then_shouldRedirectWithStatusMessage() throws Exception {
        mockMvc.perform(get("/customers/1/enabled/true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customers/page/0?sortField=firstName&sortDir=asc"));

        then(customerService).should().updateCustomerEnabledStatus(1, true);
    }

    @Test
    void given_validCustomerId_when_showCustomerDetails_then_shouldReturnCustomerDetailsView() throws Exception {
        given(customerService.get(1)).willReturn(customer);

        mockMvc.perform(get("/customers/details/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/customer_details"))
                .andExpect(model().attributeExists("customer"));

        then(customerService).should().get(1);
    }

}

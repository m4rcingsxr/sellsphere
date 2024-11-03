package com.sellsphere.client.address;

import com.sellsphere.client.customer.CustomerService;
import com.sellsphere.client.setting.CountryRepository;
import com.sellsphere.common.entity.Address;
import com.sellsphere.common.entity.AddressNotFoundException;
import com.sellsphere.common.entity.Constants;
import com.sellsphere.common.entity.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static com.sellsphere.client.address.AddressTestUtil.generateDummyAddress1;
import static com.sellsphere.client.customer.CustomerTestUtil.generateDummyCustomer;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
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

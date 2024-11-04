package com.sellsphere.client.wishlist;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sellsphere.client.customer.CustomerService;
import com.sellsphere.common.entity.Customer;
import com.sellsphere.common.entity.CustomerNotFoundException;
import com.sellsphere.common.entity.ProductNotFoundException;
import com.sellsphere.common.entity.WishlistNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class WishlistRestControllerTest {

    @MockBean
    private WishlistService wishlistService;

    @MockBean
    private CustomerService customerService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void givenValidCustomerAndProduct_whenAddProductToWishlist_thenReturnSuccessResponse() throws Exception {
        // Given
        int productId = 1;
        Principal principal = () -> "customer@example.com";
        Customer customer = new Customer();
        customer.setEmail(principal.getName());

        given(customerService.getByEmail(principal.getName())).willReturn(customer);

        // When & Then
        mockMvc.perform(post("/wishlist/add/{productId}", productId)
                                .principal(principal)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Successfully added product to wishlist"));
    }

    @Test
    void givenNonExistentCustomer_whenAddProductToWishlist_thenReturnCustomerNotFoundError() throws Exception {
        // Given
        int productId = 1;
        Principal principal = () -> "nonexistent@example.com";

        willThrow(new CustomerNotFoundException("Customer not found"))
                .given(customerService).getByEmail(principal.getName());

        // When & Then
        mockMvc.perform(post("/wishlist/add/{productId}", productId)
                                .principal(principal)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found"));
    }

    @Test
    void givenNonExistentProduct_whenAddProductToWishlist_thenReturnProductNotFoundError() throws Exception {
        // Given
        int productId = 9999;
        Principal principal = () -> "customer@example.com";
        Customer customer = new Customer();
        customer.setEmail(principal.getName());

        given(customerService.getByEmail(principal.getName())).willReturn(customer);
        willThrow(new ProductNotFoundException("Product not found"))
                .given(wishlistService).addProduct(productId, customer);

        // When & Then
        mockMvc.perform(post("/wishlist/add/{productId}", productId)
                                .principal(principal)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found"));
    }

    @Test
    void givenValidCustomerAndProduct_whenDeleteProductFromWishlist_thenReturnSuccessResponse() throws Exception {
        // Given
        int productId = 1;
        Principal principal = () -> "customer@example.com";
        Customer customer = new Customer();
        customer.setEmail(principal.getName());

        given(customerService.getByEmail(principal.getName())).willReturn(customer);

        // When & Then
        mockMvc.perform(post("/wishlist/delete/{productId}", productId)
                                .principal(principal)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Successfully deleted product from wishlist"));
    }

    @Test
    void givenNonExistentCustomer_whenDeleteProductFromWishlist_thenReturnCustomerNotFoundError() throws Exception {
        // Given
        int productId = 1;
        Principal principal = () -> "nonexistent@example.com";

        willThrow(new CustomerNotFoundException("Customer not found"))
                .given(customerService).getByEmail(principal.getName());

        // When & Then
        mockMvc.perform(post("/wishlist/delete/{productId}", productId)
                                .principal(principal)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found"));
    }

    @Test
    void givenNonExistentProduct_whenDeleteProductFromWishlist_thenReturnProductNotFoundError() throws Exception {
        // Given
        int productId = 9999;
        Principal principal = () -> "customer@example.com";
        Customer customer = new Customer();
        customer.setEmail(principal.getName());

        given(customerService.getByEmail(principal.getName())).willReturn(customer);
        willThrow(new ProductNotFoundException("Product not found"))
                .given(wishlistService).deleteProduct(productId, customer);

        // When & Then
        mockMvc.perform(post("/wishlist/delete/{productId}", productId)
                                .principal(principal)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found"));
    }

    @Test
    void givenProductNotInWishlist_whenDeleteProduct_thenReturnWishlistNotFoundError() throws Exception {
        // Given
        int productId = 1;
        Principal principal = () -> "customer@example.com";
        Customer customer = new Customer();
        customer.setEmail(principal.getName());

        given(customerService.getByEmail(principal.getName())).willReturn(customer);
        willThrow(new WishlistNotFoundException("Wishlist not found"))
                .given(wishlistService).deleteProduct(productId, customer);

        // When & Then
        mockMvc.perform(post("/wishlist/delete/{productId}", productId)
                                .principal(principal)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Wishlist not found"));
    }
}

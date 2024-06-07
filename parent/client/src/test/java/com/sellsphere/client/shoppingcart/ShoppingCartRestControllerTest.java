package com.sellsphere.client.shoppingcart;

import com.sellsphere.client.customer.CustomerService;
import com.sellsphere.client.product.ProductService;
import com.sellsphere.common.entity.*;
import com.sellsphere.common.entity.payload.MinCartItemDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ShoppingCartRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShoppingCartService cartService;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private ProductService productService;

    @Mock
    private Principal principal;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(principal.getName()).thenReturn("john.doe@example.com");
    }

    @Test
    void givenValidProduct_whenAddProductToCart_thenReturnOk() throws Exception {
        // Given
        Customer customer = new Customer();
        customer.setId(1);
        when(customerService.getByEmail(anyString())).thenReturn(customer);

        Product product = new Product();
        product.setId(1);
        when(productService.findById(anyInt())).thenReturn(product);

        // When & Then
        mockMvc.perform(post("/cart/add/{productId}/{quantity}", 1, 2)
                                .principal(principal))
                .andExpect(status().isOk());

        verify(cartService, times(1)).addProduct(customer, product, 2);
    }

    @Test
    void givenInvalidCustomer_whenAddProductToCart_thenThrowCustomerNotFoundException() throws Exception {
        // Given
        when(customerService.getByEmail(anyString())).thenThrow(new CustomerNotFoundException("Customer not found"));

        // When & Then
        mockMvc.perform(post("/cart/add/{productId}/{quantity}", 1, 2)
                                .principal(principal))
                .andExpect(status().isNotFound());

        verify(cartService, never()).addProduct(any(Customer.class), any(Product.class), anyInt());
    }

    @Test
    void givenValidRequest_whenClearCart_thenReturnOk() throws Exception {
        // Given
        Customer customer = new Customer();
        customer.setId(1);
        when(customerService.getByEmail(anyString())).thenReturn(customer);

        // When & Then
        mockMvc.perform(post("/cart/clear").principal(principal))
                .andExpect(status().isOk());

        verify(cartService, times(1)).clearCart(customer);
    }

    @Test
    void givenValidProduct_whenDeleteProductFromCart_thenReturnOk() throws Exception {
        // Given
        Customer customer = new Customer();
        customer.setId(1);
        when(customerService.getByEmail(anyString())).thenReturn(customer);

        // When & Then
        mockMvc.perform(delete("/cart/delete/{productId}", 1).principal(principal))
                .andExpect(status().isOk());

        verify(cartService, times(1)).deleteProduct(1, 1);
    }

    @Test
    void givenValidRequest_whenGetCartItems_thenReturnCartItems() throws Exception {
        // Given
        Customer customer = new Customer();
        customer.setId(1);

        Product product = new Product();
        product.setId(1);

        when(customerService.getByEmail(anyString())).thenReturn(customer);

        List<CartItem> cartItems = List.of(new CartItem(customer, product, 2));
        when(cartService.findAllByCustomer(any(Customer.class))).thenReturn(cartItems);

        // When & Then
        mockMvc.perform(get("/cart/items").principal(principal))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1));

        verify(cartService, times(1)).findAllByCustomer(customer);
    }

    @Test
    void givenValidCartItems_whenSetCart_thenReturnOk() throws Exception {
        // Given
        Customer customer = new Customer();
        customer.setId(1);
        when(customerService.getByEmail(anyString())).thenReturn(customer);

        List<MinCartItemDTO> cartItemDtos = List.of(new MinCartItemDTO(1, 2));
        String cartJson = "[{\"productId\":1,\"quantity\":2}]";

        // When & Then
        mockMvc.perform(post("/cart/set")
                                .principal(principal)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(cartJson))
                .andExpect(status().isOk());

        verify(cartService, times(1)).deleteByCustomer(customer);
        verify(cartService, times(1)).saveAll(anyList());
    }
}

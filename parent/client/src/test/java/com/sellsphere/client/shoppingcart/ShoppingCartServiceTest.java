package com.sellsphere.client.shoppingcart;

import com.sellsphere.common.entity.CartItem;
import com.sellsphere.common.entity.CartItemNotFoundException;
import com.sellsphere.common.entity.Customer;
import com.sellsphere.common.entity.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShoppingCartServiceTest {

    @InjectMocks
    private ShoppingCartService shoppingCartService;

    @Mock
    private CartItemRepository cartItemRepository;

    @Test
    void givenCustomer_whenFindAllByCustomer_thenCartItemsShouldBeFound() {
        // Given
        Customer customer = new Customer();
        customer.setId(1);
        when(cartItemRepository.findByCustomer(customer)).thenReturn(List.of(new CartItem(), new CartItem(), new CartItem()));

        // When
        List<CartItem> cartItems = shoppingCartService.findAllByCustomer(customer);

        // Then
        assertNotNull(cartItems);
        assertEquals(3, cartItems.size());
    }

    @Test
    void givenCustomerAndProduct_whenAddProduct_thenCartItemShouldBeAddedOrUpdated() throws CartItemNotFoundException {
        // Given
        Customer customer = new Customer();
        customer.setId(1);
        Product product = new Product();
        product.setId(1);
        CartItem existingCartItem = new CartItem(customer, product, 2);
        when(cartItemRepository.findByCustomerAndProduct(customer, product)).thenReturn(Optional.of(existingCartItem));

        // When
        shoppingCartService.addProduct(customer, product, 3);

        // Then
        verify(cartItemRepository).save(any(CartItem.class));
        assertEquals(3, existingCartItem.getQuantity());
    }

    @Test
    void givenCustomerAndProduct_whenAddProductWithInvalidCustomerAndProduct_thenShouldThrowException() {
        // Given
        Customer customer = new Customer();
        customer.setId(1);
        Product product = new Product();
        product.setId(1);
        when(cartItemRepository.findByCustomerAndProduct(customer, product)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CartItemNotFoundException.class, () -> shoppingCartService.addProduct(customer, product, 6));
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void givenCustomerIdAndProductId_whenDeleteProduct_thenCartItemShouldBeDeleted() {
        // When
        shoppingCartService.deleteProduct(1, 1);

        // Then
        verify(cartItemRepository).deleteByCustomerAndProduct(1, 1);
    }

    @Test
    void givenCustomer_whenClearCart_thenAllCartItemsShouldBeDeleted() {
        // Given
        Customer customer = new Customer();
        customer.setId(1);

        // When
        shoppingCartService.clearCart(customer);

        // Then
        verify(cartItemRepository).deleteAllByCustomer(customer);
    }

    @Test
    void givenCustomer_whenDeleteByCustomer_thenAllCartItemsShouldBeDeleted() {
        // Given
        Customer customer = new Customer();
        customer.setId(1);

        // When
        shoppingCartService.deleteByCustomer(customer);

        // Then
        verify(cartItemRepository).deleteAllByCustomer(customer);
    }

    @Test
    void givenNewCartItems_whenSaveAll_thenAllCartItemsShouldBeSaved() {
        // Given
        List<CartItem> newCartItems = List.of(new CartItem(), new CartItem());

        // When
        shoppingCartService.saveAll(newCartItems);

        // Then
        verify(cartItemRepository).saveAll(newCartItems);
    }
}

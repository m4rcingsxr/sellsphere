package com.sellsphere.client.shoppingcart;

import com.sellsphere.common.entity.CartItem;
import com.sellsphere.common.entity.Customer;
import com.sellsphere.common.entity.Product;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DataJpaTest
@Sql(scripts = "classpath:sql/cart_items.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Transactional
class CartItemRepositoryTest {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Test
    void givenCustomer_whenFindByCustomer_thenCartItemsShouldBeFound() {
        // Given
        Customer customer = new Customer();
        customer.setId(1);

        // When
        List<CartItem> cartItems = cartItemRepository.findByCustomer(customer);

        // Then
        assertNotNull(cartItems);
        assertEquals(3, cartItems.size());
    }

    @Test
    void givenCustomerAndProduct_whenFindByCustomerAndProduct_thenCartItemShouldBeFound() {
        // Given
        Customer customer = new Customer();
        customer.setId(1);
        Product product = new Product();
        product.setId(1);

        // When
        Optional<CartItem> cartItem = cartItemRepository.findByCustomerAndProduct(customer, product);

        // Then
        assertTrue(cartItem.isPresent());
        assertEquals(2, cartItem.get().getQuantity());
    }

    @Test
    void givenCustomerAndProduct_whenDeleteByCustomerAndProduct_thenCartItemShouldBeDeleted() {
        // Given
        Customer customer = new Customer();
        customer.setId(1);
        Product product = new Product();
        product.setId(1);

        // When
        cartItemRepository.deleteByCustomerAndProduct(1, 1);

        // Then
        Optional<CartItem> cartItem = cartItemRepository.findByCustomerAndProduct(customer, product);
        assertFalse(cartItem.isPresent());
    }

    @Test
    void givenCustomer_whenDeleteAllByCustomer_thenAllCartItemsShouldBeDeleted() {
        // Given
        Customer customer = new Customer();
        customer.setId(1);

        // When
        cartItemRepository.deleteAllByCustomer(customer);

        // Then
        List<CartItem> cartItems = cartItemRepository.findByCustomer(customer);
        assertTrue(cartItems.isEmpty());
    }

    @Test
    void givenCartItem_whenSave_thenCartItemShouldBeSaved() {
        // Given
        Customer customer = new Customer();
        customer.setId(1);
        Product product = new Product();
        product.setId(2);
        CartItem cartItem = new CartItem(customer, product, 3);

        // When
        CartItem savedCartItem = cartItemRepository.save(cartItem);

        // Then
        assertNotNull(savedCartItem);
        assertNotNull(savedCartItem.getId());
        assertEquals(3, savedCartItem.getQuantity());
    }

    @Test
    void givenExistingCartItem_whenUpdate_thenCartItemQuantityShouldBeUpdated() {
        // Given
        CartItem cartItem = cartItemRepository.findById(1).orElseThrow();
        cartItem.setQuantity(5);

        // When
        CartItem updatedCartItem = cartItemRepository.save(cartItem);

        // Then
        assertEquals(5, updatedCartItem.getQuantity());
    }

    @Test
    void givenExistingCartItem_whenDelete_thenCartItemShouldBeDeleted() {
        // When
        cartItemRepository.deleteById(1);

        // Then
        Optional<CartItem> cartItem = cartItemRepository.findById(1);
        assertFalse(cartItem.isPresent());
    }

    @Test
    void whenCount_thenTotalCartItemsShouldBeCounted() {
        // When
        long count = cartItemRepository.count();

        // Then
        assertEquals(3, count);
    }
}

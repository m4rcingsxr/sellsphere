package com.sellsphere.client.wishlist;

import com.sellsphere.client.product.ProductService;
import com.sellsphere.common.entity.*;
import org.glassfish.jersey.internal.inject.Custom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS,
    scripts = {
        "/sql/categories.sql",
        "/sql/brands.sql",
        "/sql/brands_categories.sql",
        "/sql/customers.sql",
        "/sql/products.sql",
        "/sql/wishlist.sql"
    })
class WishlistServiceIntegrationTest {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private WishlistService wishlistService;

    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setId(2); // Ensure this ID exists in customers.sql
    }

    @Test
    void givenExistingCustomerAndProduct_whenAddProduct_thenProductAddedToWishlist() throws ProductNotFoundException {
        int productId = 1; // Ensure this ID exists in products.sql
        Product product = productService.findById(productId);

        wishlistService.addProduct(productId, testCustomer);

        Optional<Wishlist> optionalWishlist = wishlistRepository.findByCustomer(testCustomer);
        assertTrue(optionalWishlist.isPresent());
        
        Wishlist wishlist = optionalWishlist.get();
        assertEquals(1, wishlist.getProducts().size());
        assertTrue(wishlist.getProducts().contains(product));
    }

    @Test
    void givenNonExistentProduct_whenAddProduct_thenThrowsProductNotFoundException() {
        int invalidProductId = 9999; // Ensure this ID does not exist

        assertThrows(ProductNotFoundException.class, () -> wishlistService.addProduct(invalidProductId, testCustomer));
    }

    @Test
    void givenExistingCustomerAndProductInWishlist_whenDeleteProduct_thenProductRemovedFromWishlist() throws ProductNotFoundException,
            WishlistNotFoundException {
        Customer customer = new Customer();
        customer.setId(1);

        int productId = 1; // Ensure this product ID exists and is already in the wishlist for the customer
        Product product = productService.findById(productId);

        wishlistService.deleteProduct(productId, customer);

        Optional<Wishlist> optionalWishlist = wishlistRepository.findByCustomer(customer);
        assertTrue(optionalWishlist.isPresent());
        
        Wishlist wishlist = optionalWishlist.get();
        assertFalse(wishlist.getProducts().contains(product));
    }

    @Test
    void givenProductNotInWishlist_whenDeleteProduct_thenThrowsProductNotFoundException() {
        int nonExistentProductId = 9999; // Ensure this product is not in the customer's wishlist

        assertThrows(ProductNotFoundException.class, () -> wishlistService.deleteProduct(nonExistentProductId, testCustomer));
    }

    @Test
    void givenExistingCustomer_whenRetrieveWishlist_thenShouldReturnWishlistWithProducts() {
        Optional<Wishlist> optionalWishlist = wishlistService.getByCustomer(testCustomer);
        assertTrue(optionalWishlist.isPresent());

        Wishlist wishlist = optionalWishlist.get();
        assertNotNull(wishlist.getCustomer());
        assertEquals(testCustomer.getId(), wishlist.getCustomer().getId());
        assertTrue(wishlist.getProducts().size() > 0, "Wishlist should contain products");
    }

    @Test
    void givenNewCustomer_whenRetrieveWishlist_thenShouldReturnEmptyOptional() {
        Customer newCustomer = new Customer();
        newCustomer.setId(9999); // Assume this ID does not exist in customers.sql

        Optional<Wishlist> optionalWishlist = wishlistService.getByCustomer(newCustomer);
        assertFalse(optionalWishlist.isPresent());
    }
}

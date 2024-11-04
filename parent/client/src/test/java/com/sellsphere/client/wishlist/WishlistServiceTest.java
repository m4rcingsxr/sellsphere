package com.sellsphere.client.wishlist;

import com.sellsphere.client.product.ProductService;
import com.sellsphere.common.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @InjectMocks
    private WishlistService wishlistService;

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private ProductService productService;

    private Customer customer;
    private Product product;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1);

        product = new Product();
        product.setId(1);
    }

    @Test
    void givenCustomerAndProduct_whenAddProduct_thenProductAddedToWishlist() throws ProductNotFoundException {
        // Given
        Wishlist wishlist = new Wishlist();
        given(wishlistRepository.findByCustomer(customer)).willReturn(Optional.of(wishlist));
        given(productService.findById(product.getId())).willReturn(product);

        // When
        wishlistService.addProduct(product.getId(), customer);

        // Then
        assertTrue(wishlist.getProducts().contains(product), "Product should be added to the wishlist");
        verify(wishlistRepository).findByCustomer(customer);
        verify(wishlistRepository).save(wishlist);
    }

    @Test
    void givenCustomerWithNoWishlist_whenAddProduct_thenCreateNewWishlistAndAddProduct() throws ProductNotFoundException {
        // Given
        int productId = 1;
        Product product = new Product();
        product.setId(productId);

        given(wishlistRepository.findByCustomer(customer)).willReturn(Optional.empty());
        given(productService.findById(productId)).willReturn(product);

        // When
        wishlistService.addProduct(productId, customer);

        // Then
        verify(wishlistRepository).save(argThat(wishlist -> wishlist.getCustomer().equals(customer) && wishlist.getProducts().contains(product)));
    }

    @Test
    void givenNonExistentProduct_whenAddProduct_thenThrowsProductNotFoundException() throws ProductNotFoundException {
        // Given
        int invalidProductId = 9999;
        given(productService.findById(invalidProductId)).willThrow(ProductNotFoundException.class);

        // When / Then
        assertThrows(ProductNotFoundException.class, () -> wishlistService.addProduct(invalidProductId, customer),
                "Should throw ProductNotFoundException if the product does not exist");
        verify(wishlistRepository, never()).save(any(Wishlist.class));
    }

    @Test
    void givenCustomerAndProductInWishlist_whenDeleteProduct_thenProductRemovedFromWishlist() throws ProductNotFoundException,
            WishlistNotFoundException {
        // Given
        Wishlist wishlist = new Wishlist();
        wishlist.addProduct(product);
        given(wishlistRepository.findByCustomer(customer)).willReturn(Optional.of(wishlist));
        given(productService.findById(product.getId())).willReturn(product);

        // When
        wishlistService.deleteProduct(product.getId(), customer);

        // Then
        assertFalse(wishlist.getProducts().contains(product), "Product should be removed from the wishlist");
        verify(wishlistRepository).findByCustomer(customer);
        verify(wishlistRepository).save(wishlist);
    }

    @Test
    void givenNonExistentWishlist_whenDeleteProduct_thenThrowsWishlistNotFoundException() {
        // Given
        given(wishlistRepository.findByCustomer(customer)).willReturn(Optional.empty());

        // When / Then
        assertThrows(WishlistNotFoundException.class, () -> wishlistService.deleteProduct(product.getId(), customer),
                "Should throw WishlistNotFoundException if the wishlist does not exist");
        verify(wishlistRepository, never()).save(any(Wishlist.class));
    }

    @Test
    void givenCustomerWithWishlist_whenGetByCustomer_thenReturnsWishlist() {
        // Given
        Wishlist wishlist = new Wishlist();
        given(wishlistRepository.findByCustomer(customer)).willReturn(Optional.of(wishlist));

        // When
        Optional<Wishlist> result = wishlistService.getByCustomer(customer);

        // Then
        assertTrue(result.isPresent(), "The customer's wishlist should be returned");
        assertEquals(wishlist, result.get(), "Returned wishlist should match the expected wishlist");
        verify(wishlistRepository).findByCustomer(customer);
    }

    @Test
    void givenCustomerWithoutWishlist_whenGetByCustomer_thenReturnsEmptyOptional() {
        // Given
        given(wishlistRepository.findByCustomer(customer)).willReturn(Optional.empty());

        // When
        Optional<Wishlist> result = wishlistService.getByCustomer(customer);

        // Then
        assertFalse(result.isPresent(), "Should return empty if the customer has no wishlist");
        verify(wishlistRepository).findByCustomer(customer);
    }
}

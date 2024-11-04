package com.sellsphere.client.wishlist;

import com.sellsphere.client.product.ProductService;
import com.sellsphere.common.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductService productService;

    public void addProduct(Integer productId, Customer customer) throws ProductNotFoundException {
        Wishlist wishlist = wishlistRepository.findByCustomer(customer)
                .orElseGet(() -> createWishlist(customer));

        Product product = productService.findById(productId);
        addProductToWishlist(wishlist, product);
    }

    private Wishlist createWishlist(Customer customer) {
        Wishlist wishlist = new Wishlist();
        wishlist.setCustomer(customer);
        return wishlist;
    }

    private void addProductToWishlist(Wishlist wishlist, Product product) {
        wishlist.addProduct(product);
        wishlistRepository.save(wishlist);
    }

    public void deleteProduct(Integer productId, Customer authenticatedCustomer)
            throws WishlistNotFoundException, ProductNotFoundException {
        Wishlist wishlist = wishlistRepository
                .findByCustomer(authenticatedCustomer)
                .orElseThrow(WishlistNotFoundException::new);

        Product product = productService.findById(productId);
        wishlist.removeProduct(product);
        wishlistRepository.save(wishlist);
    }

    public Optional<Wishlist> getByCustomer(Customer customer) {
        return wishlistRepository.findByCustomer(customer);
    }
}
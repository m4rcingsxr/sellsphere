package com.sellsphere.client.shoppingcart;

import com.sellsphere.client.product.ProductRepository;
import com.sellsphere.common.entity.*;
import com.sellsphere.common.entity.payload.MinCartItemDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Service class for handling shopping cart operations.
 * Provides methods to interact with the cart items such as adding,
 * deleting, and retrieving items for a customer.
 */
@RequiredArgsConstructor
@Transactional
@Service
public class ShoppingCartService {

    private static final int MAX_QUANTITY_PER_PRODUCT = 5;

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    /**
     * Retrieves all cart items for a given customer.
     *
     * @param customer the customer to retrieve cart items for
     * @return a list of cart items for the customer
     */
    public List<CartItem> findAllByCustomer(Customer customer) {
        return cartItemRepository.findByCustomer(customer);
    }

    /**
     * Adds a product to the cart for a given customer.
     * If the product already exists in the cart, updates the quantity.
     *
     * @param customer the customer to add the product for
     * @param productId  the product to add
     * @param quantity the quantity of the product to add
     * @throws IllegalStateException if the quantity exceeds the maximum allowed
     */
    public void addProduct(Customer customer, Integer productId, Integer quantity)
            throws ProductNotFoundException {
        Product product = productRepository
                .findById(productId)
                .orElseThrow(ProductNotFoundException::new);

        Optional<CartItem> cartItem = cartItemRepository.findByCustomerAndProduct(customer, product);

        if(quantity < 1 || quantity > MAX_QUANTITY_PER_PRODUCT) {
            throw new IllegalStateException("Max product quantity is " + MAX_QUANTITY_PER_PRODUCT);
        }

        if(cartItem.isPresent()) {
            cartItem.get().setQuantity(quantity);
        } else {
            cartItem = Optional.of(new CartItem(customer.getId(), product.getId(), quantity));
        }

        cartItemRepository.save(cartItem.get());
    }

    /**
     * Deletes a product from the cart for a given customer.
     *
     * @param customerId the ID of the customer
     * @param productId  the ID of the product to delete
     */
    public void deleteProduct(Integer customerId, Integer productId) {
        cartItemRepository.deleteByCustomerAndProduct(customerId, productId);
    }

    /**
     * Clears the entire cart for a given customer.
     *
     * @param customer the customer to clear the cart for
     */
    public void clearCart(Customer customer) {
        cartItemRepository.deleteAllByCustomer(customer);
    }

    /**
     * Deletes all cart items for a given customer.
     *
     * @param customer the customer to delete cart items for
     */
    public void deleteByCustomer(Customer customer) {
        cartItemRepository.deleteAllByCustomer(customer);
    }

    /**
     * Saves a list of cart items.
     *
     * @param newCart the list of cart items to save
     */
    public void saveAll(List<CartItem> newCart) {
        cartItemRepository.saveAll(newCart);
    }

}

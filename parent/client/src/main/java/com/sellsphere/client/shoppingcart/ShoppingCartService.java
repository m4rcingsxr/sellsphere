package com.sellsphere.client.shoppingcart;

import com.sellsphere.common.entity.CartItem;
import com.sellsphere.common.entity.CartItemNotFoundException;
import com.sellsphere.common.entity.Customer;
import com.sellsphere.common.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class for handling shopping cart operations.
 * Provides methods to interact with the cart items such as adding,
 * deleting, and retrieving items for a customer.
 */
@RequiredArgsConstructor
@Service
public class ShoppingCartService {

    private static final int MAX_QUANTITY_PER_PRODUCT = 5;

    private final CartItemRepository cartItemRepository;

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
     * @param product  the product to add
     * @param quantity the quantity of the product to add
     * @throws IllegalStateException if the quantity exceeds the maximum allowed
     */
    public void addProduct(Customer customer, Product product, Integer quantity)
            throws CartItemNotFoundException {
        CartItem cartItem = cartItemRepository.findByCustomerAndProduct(customer, product).orElseThrow(
                CartItemNotFoundException::new);

        if(quantity < 1 || quantity > MAX_QUANTITY_PER_PRODUCT) {
            throw new IllegalStateException("Max product quantity is " + MAX_QUANTITY_PER_PRODUCT);
        }

        if(cartItem != null) {
            cartItem.setQuantity(quantity);
        } else {
            cartItem = new CartItem(customer, product, quantity);
        }

        cartItemRepository.save(cartItem);
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

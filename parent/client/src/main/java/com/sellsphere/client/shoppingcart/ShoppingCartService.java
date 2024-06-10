package com.sellsphere.client.shoppingcart;

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

    private final ShoppingCartRepository shoppingCartRepository;

    /**
     * Retrieves all cart items for a given customer.
     *
     * @param customer the customer to retrieve cart items for
     * @return a list of cart items for the customer
     */
    public List<CartItem> findCartItemsByCustomer(Customer customer) {
        Optional<ShoppingCart> cart = shoppingCartRepository.findByCustomer(customer);
        if(cart.isPresent()) {
            return cart.get().getCartItems();
        } else {
            return Collections.emptyList();
        }
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
    public void addProduct(Customer customer, Product product, Integer quantity) {
        if(quantity < 1 || quantity > MAX_QUANTITY_PER_PRODUCT) {
            throw new IllegalStateException("Max product quantity is " + MAX_QUANTITY_PER_PRODUCT);
        }

        ShoppingCart customerCart = customer.getCart();

        if (customerCart == null) {
            ShoppingCart cart = new ShoppingCart();
            cart.setCustomer(customer);
            cart.addCartItem(
                    new CartItem(product, quantity)
            );

            customerCart = cart;
        } else {
            List<CartItem> cartItems = customerCart.getCartItems();
            Optional<CartItem> item = cartItems
                    .stream()
                    .filter(cartItem -> cartItem.getProduct().equals(product))
                    .findFirst();

            if(item.isPresent()) {
                CartItem cartItem = item.get();
                cartItem.setQuantity(quantity);
            } else {
                CartItem cartItem = new CartItem(product, quantity);
                customerCart.addCartItem(cartItem);
            }
        }

        shoppingCartRepository.save(customerCart);
    }

    /**
     * Deletes a product from the cart for a given customer.
     *
     * @param customerId the ID of the customer
     * @param productId  the ID of the product to delete
     * @throws ShoppingCartNotFoundException if the shopping cart is not found
     * @throws ProductNotFoundException if the product is not found
     */
    public void deleteProduct(Integer customerId, Integer productId)
            throws ShoppingCartNotFoundException, ProductNotFoundException {
        ShoppingCart shoppingCart = shoppingCartRepository.findByCustomerId(customerId)
                .orElseThrow(ShoppingCartNotFoundException::new);

        CartItem item = shoppingCart
                .getCartItems()
                .stream()
                .filter(cartItem -> cartItem.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(ProductNotFoundException::new);

        shoppingCart.removeCartItem(item);
        shoppingCartRepository.save(shoppingCart);
    }

    /**
     * Clears the entire cart for a given customer.
     *
     * @param customer the customer to clear the cart for
     */
    public void deleteCart(Customer customer) throws ShoppingCartNotFoundException {
        ShoppingCart cart = shoppingCartRepository
                .findByCustomer(customer)
                .orElseThrow(ShoppingCartNotFoundException::new);
        shoppingCartRepository.delete(cart);
    }

    public void deleteCart(String paymentIntentId) throws ShoppingCartNotFoundException {
        ShoppingCart cart = shoppingCartRepository
                .findByPaymentIntentId(paymentIntentId)
                .orElseThrow(ShoppingCartNotFoundException::new);
        shoppingCartRepository.delete(cart);
    }

    /**
     * Creates a new cart or replaces cart items for a given customer.
     *
     * @param customer the customer to create or update the cart for
     * @param newCartDto the new cart items to set
     * @return the updated or newly created shopping cart
     */
    public ShoppingCart createCart(Customer customer, List<MinCartItemDTO> newCartDto) {
        List<CartItem> newCart = newCartDto
                .stream()
                .map(item -> new CartItem(
                        new Product(item.getProductId()),
                        item.getQuantity()))
                .toList();

        Optional<ShoppingCart> cart = shoppingCartRepository.findByCustomer(customer);
        if(cart.isPresent()) {
            for (CartItem cartItem : newCart) {
                cartItem.setCart(cart.get());
            }

            cart.get().setCartItems(newCart);
            return shoppingCartRepository.save(cart.get());
        } else {
            ShoppingCart shoppingCart = new ShoppingCart();
            shoppingCart.setCustomer(customer);
            newCart.forEach(shoppingCart::addCartItem);
            return shoppingCartRepository.save(shoppingCart);
        }
    }

    public ShoppingCart findByCustomer(Customer customer) throws ShoppingCartNotFoundException {
        return shoppingCartRepository.findByCustomer(customer).orElseThrow(ShoppingCartNotFoundException::new);
    }

    public Boolean existByCustomer(Customer customer) {
        return shoppingCartRepository.existsByCustomer(customer);
    }

    public ShoppingCart save(ShoppingCart cart) {
        return shoppingCartRepository.save(cart);
    }

}

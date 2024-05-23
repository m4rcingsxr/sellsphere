package com.sellsphere.client.shoppingcart;

import com.sellsphere.client.customer.CustomerService;
import com.sellsphere.client.product.ProductService;
import com.sellsphere.common.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

/**
 * REST controller for handling shopping cart operations.
 * Provides endpoints to add, delete, clear products from the cart,
 * retrieve cart items, and set the entire cart.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/cart")
public class ShoppingCartRestController {

    private final ShoppingCartService cartService;
    private final CustomerService customerService;
    private final ProductService productService;

    /**
     * Adds a product to the cart for the authenticated customer.
     * Throws exceptions if the customer or product is not found,
     * or if the quantity exceeds the limit.
     *
     * @param productId  the ID of the product to add
     * @param quantity   the quantity of the product to add
     * @param principal  the authenticated user's principal
     * @throws CustomerNotFoundException if the customer is not found
     * @throws ProductNotFoundException  if the product is not found
     */
    @PostMapping("/add/{productId}/{quantity}")
    public void addProductToCart(@PathVariable("productId") Integer productId,
                                 @PathVariable("quantity") Integer quantity,
                                 Principal principal)
            throws CustomerNotFoundException, ProductNotFoundException, CartItemNotFoundException {
        String email = principal.getName();
        Customer customer = customerService.getByEmail(email);
        Product product = productService.findById(productId);

        cartService.addProduct(customer, product, quantity);
    }

    /**
     * Clears the cart for the authenticated customer.
     *
     * @param principal the authenticated user's principal
     * @throws CustomerNotFoundException if the customer is not found
     */
    @PostMapping("/clear")
    public void clearCart(Principal principal)
            throws CustomerNotFoundException {
        String email = principal.getName();
        Customer customer = customerService.getByEmail(email);

        cartService.clearCart(customer);
    }

    /**
     * Deletes a product from the cart for the authenticated customer.
     *
     * @param productId the ID of the product to delete
     * @param principal the authenticated user's principal
     * @throws CustomerNotFoundException if the customer is not found
     */
    @DeleteMapping("/delete/{productId}")
    public void deleteProductFromCart(
            @PathVariable("productId") Integer productId,
            Principal principal)
            throws CustomerNotFoundException {
        String email = principal.getName();
        Customer customer = customerService.getByEmail(email);

        cartService.deleteProduct(customer.getId(), productId);
    }

    /**
     * Retrieves the cart items for the authenticated customer.
     *
     * @param principal the authenticated user's principal
     * @return a list of cart items for the customer
     * @throws CustomerNotFoundException if the customer is not found
     */
    @GetMapping("/items")
    public List<CartItemDto> getCartItems(
            Principal principal)
            throws CustomerNotFoundException {
        if (principal != null) {
            String email = principal.getName();
            Customer customer = customerService.getByEmail(email);

            return cartService.findAllByCustomer(customer).stream().map(CartItemDto::new).toList();
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Sets the entire cart for the authenticated customer.
     * Deletes the current cart and saves the new one.
     *
     * @param principal the authenticated user's principal
     * @param cart      the list of cart items to set
     * @throws CustomerNotFoundException if the customer is not found
     */
    @PostMapping("/set")
    public void setCart(Principal principal,
                        @RequestBody List<CartItemDto> cart)
            throws CustomerNotFoundException {
        String email = principal.getName();
        Customer customer = customerService.getByEmail(email);
        cartService.deleteByCustomer(customer);

        List<CartItem> newCart = cart
                .stream()
                .map(
                        cartItemDTO -> new CartItem(
                                customer.getId(),
                                cartItemDTO.getProductId(),
                                cartItemDTO.getQuantity()
                )).toList();

        cartService.saveAll(newCart);
    }

}
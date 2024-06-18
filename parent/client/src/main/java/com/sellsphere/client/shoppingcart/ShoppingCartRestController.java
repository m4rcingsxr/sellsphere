package com.sellsphere.client.shoppingcart;

import com.sellsphere.client.customer.CustomerService;
import com.sellsphere.client.product.ProductService;
import com.sellsphere.common.entity.*;
import com.sellsphere.common.entity.payload.MinCartItemDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
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

    private final CustomerService customerService;
    private final ProductService productService;
    private final ShoppingCartService cartService;
    private final CartItemRepository cartItemRepository;

    /**
     * Adds a product to the cart for the authenticated customer.
     * Throws exceptions if the customer or product is not found,
     * or if the quantity exceeds the limit.
     *
     * @param productId the ID of the product to add
     * @param quantity  the quantity of the product to add
     * @param principal the authenticated user's principal
     * @throws CustomerNotFoundException if the customer is not found
     * @throws ProductNotFoundException  if the product is not found
     */
    @PostMapping("/add/{productId}/{quantity}")
    public ResponseEntity<Void> addProductToCart(@PathVariable("productId") Integer productId,
                                                 @PathVariable("quantity") Integer quantity,
                                                 Principal principal)
            throws CustomerNotFoundException, ProductNotFoundException {
        String email = principal.getName();
        Customer customer = customerService.getByEmail(email);

        cartService.addProduct(customer, productId, quantity);

        return ResponseEntity.ok().build();
    }

    /**
     * Clears the cart for the authenticated customer.
     *
     * @param principal the authenticated user's principal
     * @throws CustomerNotFoundException if the customer is not found
     */
    @PostMapping("/clear")
    public ResponseEntity<Void> clearCart(Principal principal)
            throws CustomerNotFoundException, ShoppingCartNotFoundException {
        String email = principal.getName();
        Customer customer = customerService.getByEmail(email);

        cartService.clearCart(customer);

        return ResponseEntity.ok().build();
    }

    /**
     * Deletes a product from the cart for the authenticated customer.
     *
     * @param productId the ID of the product to delete
     * @param principal the authenticated user's principal
     * @throws CustomerNotFoundException if the customer is not found
     * @throws ShoppingCartNotFoundException if the shopping cart is not found
     * @throws ProductNotFoundException if the product is not found
     */
    @DeleteMapping("/delete/{productId}")
    public ResponseEntity<Void> deleteProductFromCart(
            @PathVariable("productId") Integer productId,
            Principal principal)
            throws CustomerNotFoundException, ShoppingCartNotFoundException,
            ProductNotFoundException {
        String email = principal.getName();
        Customer customer = customerService.getByEmail(email);

        cartService.deleteProduct(customer.getId(), productId);
        return ResponseEntity.ok().build();
    }

    /**
     * Retrieves the cart items for the authenticated customer.
     *
     * @param principal the authenticated user's principal
     * @return a list of cart items for the customer
     * @throws CustomerNotFoundException if the customer is not found
     */
    @GetMapping("/items")
    public ResponseEntity<List<MinCartItemDTO>> getCartItems(
            Principal principal)
            throws CustomerNotFoundException {
        String email = principal.getName();
        Customer customer = customerService.getByEmail(email);

        List<MinCartItemDTO> list = cartService
                .findAllByCustomer(customer).stream().map(MinCartItemDTO::new).toList();
        return ResponseEntity.ok(list);
    }

    @PostMapping("/set")
    public ResponseEntity<Void> setCart(Principal principal,
                                        @RequestBody List<MinCartItemDTO> cart)
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
        return ResponseEntity.ok().build();
    }
}

package com.sellsphere.client.wishlist;

import com.sellsphere.client.customer.CustomerService;
import com.sellsphere.common.entity.Customer;
import com.sellsphere.common.entity.CustomerNotFoundException;
import com.sellsphere.common.entity.ProductNotFoundException;
import com.sellsphere.common.entity.WishlistNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RequiredArgsConstructor
@RestController
@RequestMapping("/wishlist")
public class WishlistRestController {

    private final WishlistService wishlistService;
    private final CustomerService customerService;

    @PostMapping("/add/{productId}")
    public ResponseEntity<String> addProductToWishlist(@PathVariable Integer productId, Principal principal)
            throws CustomerNotFoundException, ProductNotFoundException {
        wishlistService.addProduct(productId, getAuthenticatedCustomer(principal));
        return ResponseEntity.ok("Successfully added product to wishlist");
    }

    @PostMapping("/delete/{productId}")
    public ResponseEntity<String> deleteProductFromWishlist(@PathVariable Integer productId, Principal principal)
            throws CustomerNotFoundException, WishlistNotFoundException, ProductNotFoundException {
        wishlistService.deleteProduct(productId, getAuthenticatedCustomer(principal));
        return ResponseEntity.ok("Successfully deleted product from wishlist");
    }

    private Customer getAuthenticatedCustomer(Principal principal) throws CustomerNotFoundException {
        return customerService.getByEmail(principal.getName());
    }

}

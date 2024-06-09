package com.sellsphere.client.checkout;

import com.sellsphere.client.customer.CustomerService;
import com.sellsphere.client.shoppingcart.ShoppingCartService;
import com.sellsphere.common.entity.Customer;
import com.sellsphere.common.entity.CustomerNotFoundException;
import com.sellsphere.common.entity.ShoppingCartNotFoundException;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/checkout")
public class CheckoutController {

    private final CustomerService customerService;
    private final ShoppingCartService cartService;

    @GetMapping
    public String checkout(Principal principal)
            throws CustomerNotFoundException, ShoppingCartNotFoundException {
        Customer authenticatedCustomer = customerService.getByEmail(principal.getName());
        if(!cartService.existByCustomer(authenticatedCustomer)) {
            throw new ShoppingCartNotFoundException();
        }
        return "checkout/checkout";
    }

    @GetMapping("/return")
    public String returnCheckout() {
        return "checkout/return";
    }

}

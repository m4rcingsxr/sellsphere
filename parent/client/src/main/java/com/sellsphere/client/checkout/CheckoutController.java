package com.sellsphere.client.checkout;

import com.sellsphere.common.entity.CustomerNotFoundException;
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

    @GetMapping
    public String checkout() {
        return "checkout/checkout";
    }

    @GetMapping("/return")
    public String returnCheckout() {
        return "checkout/return";
    }

}

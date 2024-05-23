package com.sellsphere.client.shoppingcart;

import com.sellsphere.client.customer.CustomerService;
import com.sellsphere.common.entity.Customer;
import com.sellsphere.common.entity.CustomerNotFoundException;
import com.sellsphere.common.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

/**
 * Controller class for handling shopping cart related web requests.
 * Provides endpoints to view the shopping cart and related details.
 */
@RequiredArgsConstructor
@Controller
@RequestMapping("/cart")
public class ShoppingCartController {

    private final CustomerService customerService;

    /**
     * Displays the shopping cart view.
     * Retrieves the customer's default address and shipping rate based on that address.
     *
     * @param principal the authenticated user's principal
     * @param model     the Spring MVC model to which attributes can be added
     * @return the view name for the shopping cart page
     * @throws CustomerNotFoundException if the customer is not found
     */
    @GetMapping
    public String viewCart(Principal principal, Model model)
            throws CustomerNotFoundException {

        if (principal != null) {
            String email = principal.getName();
            Customer customer = customerService.getByEmail(email);

            model.addAttribute("addresses", customer.getAddresses());
        }

        return "cart/shopping_cart";
    }
}

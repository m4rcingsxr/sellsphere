package com.sellsphere.client.wishlist;

import com.sellsphere.client.customer.CustomerService;
import com.sellsphere.common.entity.Customer;
import com.sellsphere.common.entity.CustomerNotFoundException;
import com.sellsphere.common.entity.Wishlist;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping("/wishlist")
public class WishlistController {

    private final CustomerService customerService;
    private final WishlistService wishlistService;

    public WishlistController(CustomerService customerService, WishlistService wishlistService) {
        this.customerService = customerService;
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public String showWishlist(Model model, Principal principal) throws CustomerNotFoundException {
        Customer customer = getAuthenticatedCustomer(principal);
        Optional<Wishlist> wishlist = wishlistService.getByCustomer(customer);
        wishlist.ifPresent(list -> {
            list.getProducts().forEach(product -> product.isOnTheWishlist(customer));
            model.addAttribute("wishlist", list);
        });

        model.addAttribute("customer", customer);
        return "wishlist/wishlist";
    }

    private Customer getAuthenticatedCustomer(Principal principal) throws CustomerNotFoundException {
        return customerService.getByEmail(principal.getName());
    }

}

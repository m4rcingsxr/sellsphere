package com.sellsphere.client.customer;

import com.sellsphere.common.entity.Customer;
import com.sellsphere.common.entity.CustomerNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@RequiredArgsConstructor
@Controller
@RequestMapping("/customer")
public class CustomerController {

    public static final String CUSTOMER_FORM_URL = "customer/customer";
    private static final String MAIN_PAGE_REDIRECT_URL = "redirect:/";

    private final CustomerService customerService;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class,
                                    new StringTrimmerEditor(true)
        );
    }

    /**
     * Display the customer dashboard or redirect to the logged-in view based on the first login flag.
     *
     * @param customer  Customer model attribute
     * @param principal Principal instance
     * @param model     Model instance
     * @return View name
     */
    @GetMapping
    public String showCustomerDetails(
            @ModelAttribute("customer") Customer customer,
            Principal principal, Model model)
            throws CustomerNotFoundException {

        String email = principal.getName();
        Customer fetchedCustomer = customerService.getByEmail(email);
        model.addAttribute("customer", fetchedCustomer);

        return CUSTOMER_FORM_URL;
    }
}

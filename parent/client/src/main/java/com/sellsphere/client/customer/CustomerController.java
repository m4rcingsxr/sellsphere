package com.sellsphere.client.customer;

import com.sellsphere.client.RecaptchaVerificationService;
import com.sellsphere.common.entity.Constants;
import com.sellsphere.common.entity.Customer;
import com.sellsphere.common.entity.CustomerNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;

@RequiredArgsConstructor
@Controller
@RequestMapping("/customer")
public class CustomerController {


    @Value("${base.url}")
    private String baseUrl;

    @Value("${keycloak.url}")
    private String keycloakUrl;

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
        model.addAttribute("baseUrl", baseUrl);
        model.addAttribute("keycloakUrl", keycloakUrl);

        return "customer/customer";
    }

    /**
     * Update the customer profile.
     *
     * @param customer Customer model attribute
     * @return View name
     */
    @PostMapping("/update")
    public String updateCustomer(
            @ModelAttribute("customer") Customer customer, RedirectAttributes ra,
            @RequestParam("g-recaptcha-response") String token)
            throws CustomerNotFoundException, RecaptchaVerificationFailed, IOException {
        RecaptchaVerificationService recaptchaService = new RecaptchaVerificationService();
        RecaptchaVerificationService.VerificationResult result = recaptchaService.verifyToken(token);
        recaptchaService.validate(result);

        customerService.updateDetails(customer);
        ra.addFlashAttribute(Constants.SUCCESS_MESSAGE, "Profile successfully updated.");

        return "redirect:/customer";
    }

}

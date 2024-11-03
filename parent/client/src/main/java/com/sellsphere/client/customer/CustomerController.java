package com.sellsphere.client.customer;

import com.sellsphere.client.KeycloakService;
import com.sellsphere.common.entity.Constants;
import com.sellsphere.common.entity.Customer;
import com.sellsphere.common.entity.CustomerNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Map;

@RequiredArgsConstructor
@Controller
@RequestMapping("/customer")
public class CustomerController {

    private static final String RECAPTCHA_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    private final CustomerService customerService;
    private final KeycloakService keycloakService;

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
            @ModelAttribute("customer") Customer customer, RedirectAttributes ra, @RequestParam("g-recaptcha-response") String recaptchaResponse)
            throws CustomerNotFoundException, RecaptchaVerificationFailed {
        String recaptchaSecret = System.getenv("RECAPTCHA_SECRET");
        String url = RECAPTCHA_VERIFY_URL + "?secret=" + recaptchaSecret + "&response=" + recaptchaResponse;

        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> response = restTemplate.postForObject(url, null, Map.class);

        if(Boolean.FALSE.equals(response.get("success"))) {
            throw new RecaptchaVerificationFailed("/customer", "Recaptcha verification failed");
        }

        customerService.update(customer);
        ra.addFlashAttribute(Constants.SUCCESS_MESSAGE, "Profile successfully updated.");

        return "redirect:/customer";
    }

}

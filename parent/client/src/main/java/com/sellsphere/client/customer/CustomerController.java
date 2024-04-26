package com.sellsphere.client.customer;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@Controller
@RequestMapping("/customer")
public class CustomerController {

    private static final String MAIN_PAGE_REDIRECT_URL = "redirect:/";
    private static final String CUSTOMER_FORM_URL = "customer/customer";

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class,
                                    new StringTrimmerEditor(true)
        );
    }


    @GetMapping
    public String showCustomerDetails() {
        return "customer/customer_form";
    }
}

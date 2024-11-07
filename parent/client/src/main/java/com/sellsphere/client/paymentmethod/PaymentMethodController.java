package com.sellsphere.client.paymentmethod;

import com.sellsphere.client.customer.CustomerService;
import com.sellsphere.common.entity.Card;
import com.sellsphere.common.entity.Customer;
import com.sellsphere.common.entity.CustomerNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("payment_methods")
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;
    private final CustomerService customerService;

    @GetMapping
    public String showPaymentMethods(Model model, Principal principal) throws CustomerNotFoundException {

        Customer customer = getAuthenticatedCustomer(principal);

        List<Card> cardList = paymentMethodService.listAllCardsByCustomer(customer);

        model.addAttribute("customer", customer);
        model.addAttribute("cardList", cardList);

        return "paymentmethods/payment_methods";
    }

    private Customer getAuthenticatedCustomer(Principal principal) throws CustomerNotFoundException {
        return customerService.getByEmail(principal.getName());
    }


}

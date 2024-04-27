package com.sellsphere.client.address;

import com.sellsphere.client.customer.CustomerService;
import com.sellsphere.client.setting.CountryRepository;
import com.sellsphere.common.entity.Address;
import com.sellsphere.common.entity.Country;
import com.sellsphere.common.entity.Customer;
import com.sellsphere.common.entity.CustomerNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;

import java.security.Principal;
import java.util.List;

/**
 * Controller class handling address-related operations such as
 * displaying address book, showing address form, saving addresses,
 * setting primary address, and deleting addresses.
 */
@Controller
@RequiredArgsConstructor
public class AddressController {

    private final CustomerService customerService;
    private final CountryRepository countryRepository;

    private static final String ADDRESSES_URL = "/address/addresses";
    private static final String ADDRESS_BOOK_DEFAULT_REDIRECT_URL = "redirect:/address_book";

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        dataBinder.registerCustomEditor(String.class,
                                        new StringTrimmerEditor(true)
        );
    }

    /**
     * Displays the address book for the authenticated customer.
     *
     * @param model the model to add attributes
     * @param principal the authenticated user's principal
     * @return the address book view
     * @throws CustomerNotFoundException if the customer is not found
     */
    @GetMapping("/address_book")
    public String showAddressBook(
            Model model, Principal principal) throws CustomerNotFoundException {
        String email = principal.getName();
        Customer customer = customerService.getByEmail(email);

        Sort sort = Sort.by("name").ascending();
        List<Country> countryList = countryRepository.findAll(sort);

        model.addAttribute("customer", customer);
        model.addAttribute("countryList", countryList);
        model.addAttribute("address", new Address());
        
        return ADDRESSES_URL;
    }

}

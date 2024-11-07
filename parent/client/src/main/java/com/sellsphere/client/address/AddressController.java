package com.sellsphere.client.address;

import com.sellsphere.client.RecaptchaVerificationService;
import com.sellsphere.client.customer.CustomerService;
import com.sellsphere.client.customer.RecaptchaVerificationFailed;
import com.sellsphere.client.setting.CountryRepository;
import com.sellsphere.common.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

/**
 * Controller class handling address-related operations such as
 * displaying address book, showing address form, saving addresses,
 * setting primary address, and deleting addresses.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/address_book")
public class AddressController {

    private final CustomerService customerService;
    private final AddressService addressService;
    private final CountryRepository countryRepository;

    private static final String ADDRESSES_URL = "address/addresses";
    public static final String ADDRESS_BOOK_DEFAULT_REDIRECT_URL = "redirect:/address_book";

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
    @GetMapping
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

    @PostMapping("/update")
    public String updateAddresses(@ModelAttribute("customer") Customer customer,
                                  RedirectAttributes ra)
            throws CustomerNotFoundException {
        String successMessage = "Successfully updated addresses";

        // Retrieve the existing customer from the database
        Customer existingCustomer = customerService.getById(customer.getId());

        // Identify and store the old primary address
        Address oldPrimary = existingCustomer.getAddresses().stream()
                .filter(Address::isPrimary)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No primary address found for existing customer"));

        // Count the primary addresses in the incoming customer object
        long primaryCount = customer.getAddresses().stream()
                .filter(Address::isPrimary)
                .count();

        if (primaryCount > 1) {
            // More than one primary address - unselect the old one
            customer.getAddresses().stream()
                    .filter(address -> address.getId().equals(oldPrimary.getId()))
                    .findFirst()
                    .ifPresent(address -> address.setPrimary(false));
        }

        // Persist changes using customerService
        customerService.update(customer);

        ra.addFlashAttribute(Constants.SUCCESS_MESSAGE, successMessage);

        return ADDRESS_BOOK_DEFAULT_REDIRECT_URL;
    }



    @PostMapping("/save")
    public String saveAddress(@ModelAttribute("address") Address address,
                              RedirectAttributes ra, @RequestParam("g-recaptcha-response") String token)
            throws RecaptchaVerificationFailed, IOException {
        RecaptchaVerificationService recaptchaService = new RecaptchaVerificationService();
        RecaptchaVerificationService.VerificationResult result = recaptchaService.verifyToken(token);
        recaptchaService.validate(result);

        String successMessage = "Successfully " + (address.getId() != null ?
                "updated" : "saved") + " new address.";

        addressService.save(address);

        ra.addFlashAttribute(Constants.SUCCESS_MESSAGE, successMessage);

        return ADDRESS_BOOK_DEFAULT_REDIRECT_URL;
    }

    /**
     * Deletes the address for the authenticated customer.
     *
     * @param addressId the address ID to delete
     * @return the address book redirect view
     * @throws AddressNotFoundException if the address is not found
     */
    @GetMapping("/delete/{id}")
    public String deleteAddress(@PathVariable("id") Integer addressId, RedirectAttributes ra)
            throws AddressNotFoundException {
        addressService.delete(addressId);
        ra.addFlashAttribute(Constants.SUCCESS_MESSAGE, "Successfully deleted address");

        return ADDRESS_BOOK_DEFAULT_REDIRECT_URL;
    }

}

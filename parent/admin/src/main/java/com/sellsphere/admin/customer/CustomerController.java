package com.sellsphere.admin.customer;

import com.sellsphere.admin.ValidationHelper;
import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.admin.page.PagingAndSortingParam;
import com.sellsphere.admin.setting.CountryRepository;
import com.sellsphere.common.entity.Constants;
import com.sellsphere.common.entity.Country;
import com.sellsphere.common.entity.Customer;
import com.sellsphere.common.entity.CustomerNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller class for managing customer-related operations, such as listing, editing, deleting, and updating customers.
 */
@RequiredArgsConstructor
@Controller
public class CustomerController {

    public static final String DEFAULT_REDIRECT_URL = "redirect:/customers/page/0?sortField=firstName&sortDir=asc";
    public static final String CUSTOMER_FORM_VIEW = "customer/customer_form";

    private final CustomerService customerService;
    private final CountryRepository countryRepository;

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        dataBinder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    /**
     * Redirects to the first page of the customer list.
     *
     * @return the redirect URL to the first page of customer list.
     */
    @GetMapping("/customers")
    public String redirectToFirstPage() {
        return DEFAULT_REDIRECT_URL;
    }

    /**
     * Lists customers by page with sorting and pagination.
     *
     * @param helper  the PagingAndSortingHelper for managing pagination and sorting
     * @param pageNum the page number to display
     * @return the view name for the paginated customer list
     */
    @GetMapping("/customers/page/{pageNum}")
    public String listCustomersByPage(
            @PagingAndSortingParam(listName = "customerList", moduleURL = "/customers") PagingAndSortingHelper helper,
            @PathVariable("pageNum") int pageNum) {
        customerService.listPage(pageNum, helper);
        return "customer/customers";
    }

    /**
     * Displays the form for editing an existing customer.
     *
     * @param id    the customer ID to edit
     * @param model the model to hold form data
     * @return the view name for the customer form
     * @throws CustomerNotFoundException if the customer is not found
     */
    @GetMapping("/customers/edit/{id}")
    public String showCustomerForm(@PathVariable("id") Integer id, Model model) throws CustomerNotFoundException {
        prepareModelWithCountries(model);
        Customer customer = customerService.get(id);
        model.addAttribute("customer", customer);
        return CUSTOMER_FORM_VIEW;
    }

    /**
     * Handles the update of customer details.
     *
     * @param customer the customer object from the form
     * @param bindingResult the result of form validation
     * @param model the model for holding form data
     * @param redirectAttributes attributes for storing success message
     * @return the redirect URL to the customer list
     * @throws CustomerNotFoundException if the customer is not found
     */
    @PostMapping("/customers/update")
    public String updateCustomer(@Valid @ModelAttribute("customer") Customer customer, BindingResult bindingResult,
                                 Model model, RedirectAttributes redirectAttributes) throws CustomerNotFoundException {
        ValidationHelper validationHelper = new ValidationHelper(bindingResult, "error.customer");
        validationHelper.validatePassword(customer.getPassword(), customer.getId());

        if (!validationHelper.validate()) {
            prepareModelWithCountries(model);
            return CUSTOMER_FORM_VIEW;
        }

        customerService.save(customer);
        redirectAttributes.addFlashAttribute(Constants.SUCCESS_MESSAGE, "Customer successfully updated");

        return DEFAULT_REDIRECT_URL + "&keyword=" + customer.getEmail().split("@")[0];
    }

    /**
     * Deletes a customer by ID.
     *
     * @param id the customer ID
     * @param redirectAttributes attributes for storing success message
     * @return the redirect URL to the customer list
     * @throws CustomerNotFoundException if the customer is not found
     */
    @GetMapping("/customers/delete/{id}")
    public String deleteCustomer(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes)
            throws CustomerNotFoundException {
        customerService.delete(id);
        redirectAttributes.addFlashAttribute(Constants.SUCCESS_MESSAGE, "Customer successfully removed.");
        return DEFAULT_REDIRECT_URL;
    }

    /**
     * Updates the enabled/disabled status of a customer.
     *
     * @param id the customer ID
     * @param enabled the new enabled status
     * @param redirectAttributes attributes for storing status update message
     * @return the redirect URL to the customer list
     * @throws CustomerNotFoundException if the customer is not found
     */
    @GetMapping("/customers/{id}/enabled/{status}")
    public String updateCustomerEnabledStatus(@PathVariable("id") Integer id,
                                              @PathVariable("status") boolean enabled,
                                              RedirectAttributes redirectAttributes) throws CustomerNotFoundException {
        customerService.updateCustomerEnabledStatus(id, enabled);
        String statusMessage = "The Customer ID " + id + " has been " + (enabled ? "enabled" : "disabled");
        redirectAttributes.addFlashAttribute(Constants.SUCCESS_MESSAGE, statusMessage);
        return DEFAULT_REDIRECT_URL;
    }

    /**
     * Displays customer details.
     *
     * @param id    the customer ID
     * @param model the model for holding customer details
     * @return the view name for customer details modal
     * @throws CustomerNotFoundException if the customer is not found
     */
    @GetMapping("/customers/details/{id}")
    public String showCustomerDetails(@PathVariable("id") Integer id, Model model) throws CustomerNotFoundException {
        Customer customer = customerService.get(id);
        model.addAttribute("customer", customer);
        return "customer/customer_details";
    }

    /**
     * Prepares the model with a list of countries for use in the customer form.
     *
     * @param model the model to populate with country data
     */
    private void prepareModelWithCountries(Model model) {
        List<Country> countries = countryRepository.findAllByOrderByName();
        model.addAttribute("countryList", countries);
    }
}

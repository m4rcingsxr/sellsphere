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
 * Controller for handling customer-related actions including listing customers,
 * showing details for editing, updating customer information, deleting
 * customers,and changing the enabled status of a customer.
 */
@RequiredArgsConstructor
@Controller
public class CustomerController {

    public static final String DEFAULT_REDIRECT_URL = "redirect:/customers" +
            "/page/0?sortField=firstName&sortDir=asc";
    private static final String CUSTOMER_FORM = "customer/customer_form";

    private final CustomerService customerService;
    private final CountryRepository countryRepository;

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        dataBinder.registerCustomEditor(String.class,
                                        new StringTrimmerEditor(true)
        );
    }

    /**
     * Redirects to the first page of the customer list with default sorting
     * parameters.
     *
     * @return A redirection string to the default sorted list of customers.
     */
    @GetMapping("/customers")
    public String listFirstPage() {
        return DEFAULT_REDIRECT_URL;
    }

    /**
     * Lists customers on a specific page number with sorting, search keyword
     * and pagination.
     *
     * @param helper  A helper object containing pagination and sorting logic.
     * @param pageNum The current page number to display.
     * @return The view name of the customers list page.
     */
    @GetMapping("/customers/page/{pageNum}")
    public String listPage(
            @PagingAndSortingParam(listName = "customerList", moduleURL =
                    "/customers") PagingAndSortingHelper helper,
            @PathVariable(name = "pageNum") int pageNum
    ) {
        customerService.listPage(pageNum, helper);

        return "customer/customers";
    }

    /**
     * Shows the form for editing an existing customer's details.
     *
     * @param id    The ID of the customer to edit.
     * @param model The Spring MVC {@link Model} object for passing data to
     *              the view.
     * @return The path to the customer edit form view.
     * @throws CustomerNotFoundException If the customer with the specified
     * ID is not found.
     */
    @GetMapping("/customers/edit/{id}")
    public String showCustomerForm(@PathVariable("id") Integer id, Model model)
            throws CustomerNotFoundException {
        prepareModelAttributesForCustomerForm(model);

        Customer customer = customerService.get(id);
        model.addAttribute("customer", customer);


        return CUSTOMER_FORM;
    }

    /**
     * Processes the form submission for updating an existing customer.
     *
     * @param customer The {@link Customer} object populated from the form data.
     * @param ra       A {@link RedirectAttributes} object for passing
     *                 attributes to the redirect target.
     * @return A redirection string to the edited customer's in customers page.
     * @throws CustomerNotFoundException If the update operation cannot
     * proceed due to the customer not being found.
     */
    @PostMapping("/customers/update")
    public String updateCustomer(@Valid @ModelAttribute("customer") Customer customer,
                                 BindingResult bindingResult, Model model,
                                 RedirectAttributes ra)
            throws CustomerNotFoundException {
        ValidationHelper validationHelper = new ValidationHelper(bindingResult, "error.customer");
        validationHelper.validatePassword(customer.getPassword(), customer.getId());

        if(!validationHelper.validate()) {
            prepareModelAttributesForCustomerForm(model);
            return CUSTOMER_FORM;
        }

        customerService.save(customer);

        ra.addFlashAttribute(Constants.SUCCESS_MESSAGE, "Customer successfully updated");
        return DEFAULT_REDIRECT_URL + "&keyword=" + customer.getEmail().split("@")[0];
    }

    private void prepareModelAttributesForCustomerForm(Model model) {
        List<Country> countryList = countryRepository.findAllByOrderByName();
        model.addAttribute("countryList", countryList);

    }
}

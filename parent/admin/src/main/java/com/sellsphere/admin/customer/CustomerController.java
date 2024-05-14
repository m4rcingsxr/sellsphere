package com.sellsphere.admin.customer;


import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.admin.page.PagingAndSortingParam;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;

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

    private final CustomerService customerService;

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

}

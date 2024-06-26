package com.sellsphere.client.order;

import com.sellsphere.client.customer.CustomerService;
import com.sellsphere.common.entity.Customer;
import com.sellsphere.common.entity.CustomerNotFoundException;
import com.sellsphere.common.entity.Order;
import com.sellsphere.common.entity.OrderNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

/**
 * Controller class handling order-related operations such as
 * listing orders, paging through orders, and viewing order details.
 */
@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CustomerService customerService;

    /**
     * Redirects to the default customer orders page.
     *
     * @return default orders redirect URL
     */
    @GetMapping("/orders")
    public String listFirstPage(Model model, Principal principal)
            throws CustomerNotFoundException {
        return listByPage(model, principal, 0, "orderTime", "asc", null);
    }

    /**
     * Lists orders by page for the authenticated customer.
     *
     * @param model the model to add attributes
     * @param principal the authenticated user's principal
     * @param pageNumber the page number to display
     * @param sortField the field to sort by
     * @param sortDir the direction to sort in
     * @param keyword the keyword to search for in order details (optional)
     * @return orders view for the specified page
     * @throws CustomerNotFoundException if the customer is not found
     */
    @GetMapping("/orders/page/{pageNum}")
    public String listByPage(Model model,
                             Principal principal,
                             @PathVariable("pageNum") Integer pageNumber,
                             @RequestParam("sortField") String sortField,
                             @RequestParam("sortDir") String sortDir,
                             @RequestParam(value = "keyword", required =
                                     false) String keyword)
            throws CustomerNotFoundException {
        String email = principal.getName();
        Customer customer = customerService.getByEmail(email);

        Page<Order> page = orderService.listByPage(customer, pageNumber, sortField, sortDir, keyword);

        model.addAttribute("currentPage", pageNumber);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());
        model.addAttribute("orderList", page.getContent());
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reversedSortDir", sortDir.equals("asc") ? "desc" : "asc");
        model.addAttribute("keyword", keyword);
        model.addAttribute("customer", customer);

        return "order/orders_customer";
    }


}

package com.sellsphere.admin.order;

import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.admin.page.PagingAndSortingParam;
import com.sellsphere.common.entity.Constants;
import com.sellsphere.common.entity.Order;
import com.sellsphere.common.entity.OrderNotFoundException;
import com.sellsphere.common.entity.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    public static final String DEFAULT_REDIRECT_URL = "redirect:/orders/page/0?sortField=orderTime&sortDir=desc";

    private final OrderService orderService;

    @GetMapping
    public String listFirstPage() {
        return DEFAULT_REDIRECT_URL;
    }

    @GetMapping("/page/{page}")
    public String listPage(
            @PagingAndSortingParam(listName = "orderList", moduleURL = "/orders") PagingAndSortingHelper helper,
            @PathVariable Integer page) {
        orderService.listPage(page, helper);
        return "order/orders";
    }

    @GetMapping("/edit/{id}")
    public String showOrderForm(@PathVariable Integer id, Model model)
            throws OrderNotFoundException {
        Order order = orderService.getById(id);

        model.addAttribute("order", order);
        model.addAttribute("orderStatusList", OrderStatus.values());
        model.addAttribute("orderStatusMap", OrderStatus.getOrderStatusMap());

        return "order/order_form";
    }

    @GetMapping("/details/{id}")
    public String showOrderDetails(@PathVariable Integer id, Model model) throws OrderNotFoundException {
        model.addAttribute("order", orderService.getById(id));
        return "order/order_detail_modal";
    }

    @PostMapping("/save")
    public String saveOrder(Order order) {
        return DEFAULT_REDIRECT_URL;
    }

    @GetMapping("{id}/status/{status}")
    public String setOrderStatus(@PathVariable Integer id, @PathVariable String status, RedirectAttributes ra)
            throws OrderNotFoundException {
        ra.addFlashAttribute(Constants.SUCCESS_MESSAGE, "Order #" + id + " has been updated to status:" + status);
        orderService.setOrderStatus(id, OrderStatus.valueOf(status));
        return DEFAULT_REDIRECT_URL;
    }
}

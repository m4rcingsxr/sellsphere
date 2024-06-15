package com.sellsphere.admin.order;

import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.admin.page.PagingAndSortingParam;
import com.sellsphere.common.entity.Order;
import com.sellsphere.easyship.EasyshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    public static final String DEFAULT_REDIRECT_URL = "redirect:/orders/page/0?sortField=orderTime&sortDir=desc";

    private final OrderService orderService;
    private final EasyshipService easyshipService;

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
    public String showOrderForm(@PathVariable Integer id, Model model) {
        return "order/order_form";
    }

    @GetMapping("/details/{id}")
    public String showOrderDetails(@PathVariable Integer id, Model model) {
        return "order/order_detail_modal";
    }

    @PostMapping("/save")
    public String saveOrder(Order order) {
        return DEFAULT_REDIRECT_URL;
    }

    // before buying label - available to modify
    @PostMapping("/delete-product")
    public String deleteProduct(@RequestParam("orderId") Integer orderId) {
        return DEFAULT_REDIRECT_URL;
    }

    @GetMapping("/buy-label")
    public String buyLabel(
            @RequestParam("shipmentId") String shipmentId,
            @RequestParam("courierId") String courierId) {

        return DEFAULT_REDIRECT_URL;
    }

    @PostMapping("/select-pickup")
    public String selectPickup(@RequestParam("orderId") Integer orderId) {
        return DEFAULT_REDIRECT_URL;
    }

    // track
    // label, status
    // courier name/logo
    // ship price
    // buy label
    // select pickup

    // functionality:
    // create label (asynchronous) - paid for shipment (final: get money from stripe)
    // show ui loading
    // wait for the webhook (label generated)
    // update ui  show link to download pdf
    // add possibility to select pickup
    // select pickup (From to time)
    // return order - generate return order - send to the client - he send the request
    //                i accept the return request generate return label - give it to the client
    //              - Return money to the client after i recieve items
    //              - Client must pay for return shipment


    // email send after buying with invoice
    // invoice + order details

}

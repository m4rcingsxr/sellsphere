package com.sellsphere.admin.transaction;

import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.admin.page.PagingAndSortingParam;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/transactions")
public class TransactionController {

    // cancel available:
    /**
     * requires_payment_method
     * requires_capture
     * requires_confirmation
     * requires_action
     * processing
     *
     * // cancel refunds
     */

    public static final String DEFAULT_REDIRECT_URL = "redirect:/transactions/page/0?sortField=created&sortDir=desc";
    private final TransactionService transactionService;

    @GetMapping
    public String showFirstPage() {
        return DEFAULT_REDIRECT_URL;
    }

    // list transactions (view taxes, fees)
    @GetMapping("/page/{page}")
    public String listTransactions(
            @PagingAndSortingParam(moduleURL = "/transactions", listName = "transactionList")
            PagingAndSortingHelper helper, @PathVariable("page") Integer page) {
        transactionService.listEntities(helper, page);

        return "transaction/transactions";
    }

    // refund transaction (or partially)


    // cancel transaction - change transaction status (possible before it's confirmed)


}

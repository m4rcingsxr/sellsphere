package com.sellsphere.admin.transaction;

import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.admin.page.PagingAndSortingParam;
import com.sellsphere.common.entity.PaymentIntent;
import com.sellsphere.common.entity.TransactionNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/transactions")
public class TransactionController {

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

    @GetMapping("/details/{id}")
    public String showTransactionDetails(@PathVariable("id") Integer id, Model model)
            throws TransactionNotFoundException {
        PaymentIntent transaction = transactionService.findById(id);
        model.addAttribute("transaction", transaction);

        return "transaction/transaction_details";
    }

    // cancel transaction - change transaction status (possible before it's confirmed)


}

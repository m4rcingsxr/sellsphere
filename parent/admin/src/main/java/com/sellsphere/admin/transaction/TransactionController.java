package com.sellsphere.admin.transaction;

import com.sellsphere.admin.export.ExportUtil;
import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.admin.page.PagingAndSortingParam;
import com.sellsphere.common.entity.PaymentIntent;
import com.sellsphere.common.entity.TransactionNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;

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
        PaymentIntent transaction = transactionService.findTransactionById(id);
        model.addAttribute("transaction", transaction);

        return "transaction/transaction_details";
    }

    @GetMapping("/export/{format}")
    public void exportTransactions(@PathVariable String format, HttpServletResponse response)
            throws IOException {
        String[] headers = {
                "Transaction ID",
                "Customer",
                "Amount",
                "Currency",
                "Status",
                "Created",
                "Fees",
                "Net Amount",
                "Shipping Amount",
                "Tax Amount",
                "Refunded Amount"
        };

        Function<PaymentIntent, String[]> extractor = transaction -> new String[]{
                transaction.getStripeId(),
                transaction.getCustomer() != null ? transaction.getCustomer().getEmail() : "N/A",
                transaction.getDisplayAmountString(),
                transaction.getTargetCurrency().getCode(),
                transaction.getDisplayStatus(),
                LocalDateTime.ofInstant(Instant.ofEpochMilli(transaction.getCreated()), ZoneId.of("Europe/Paris")).format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                transaction.getDisplayFee().toString(),
                transaction.getDisplayNet().toString(),
                transaction.getDisplayShippingAmount().toString(),
                transaction.getDisplayTax().toString(),
                transaction.getDisplayRefundedString()
        };

        ExportUtil.export(format, this::listAllTransactions, headers, extractor, response);
    }

    private List<PaymentIntent> listAllTransactions() {
        return transactionService.findAll();
    }

}

package com.sellsphere.admin.transaction;

import com.sellsphere.common.entity.Refund;
import com.sellsphere.common.entity.TransactionNotFoundException;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/transactions")
public class TransactionRestController {

    private final TransactionService transactionService;

    @PostMapping("/refund")
    public ResponseEntity<Map<String, String>> requestRefund(@RequestBody RefundDTO refundDTO)
            throws StripeException, TransactionNotFoundException {

        // validate if refund exceed total amount (server + client - rest api call)
        String status = transactionService.createRefund(refundDTO.getPaymentIntent(),
                                                        refundDTO.getAmount(),
                                                        refundDTO.getReason());
        Map<String, String> map = new HashMap<>();
        map.put("status", status);

        return ResponseEntity.ok(map);
    }

}

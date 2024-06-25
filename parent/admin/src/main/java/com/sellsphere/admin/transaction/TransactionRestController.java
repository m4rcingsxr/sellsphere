package com.sellsphere.admin.transaction;

import com.sellsphere.common.entity.TransactionNotFoundException;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/transactions")
public class TransactionRestController {

    private final TransactionService transactionService;

    @PostMapping("/refund")
    public ResponseEntity<Map<String, Object>> requestRefund(@RequestBody RefundDTO refundDTO)
            throws StripeException, TransactionNotFoundException {

        BigDecimal availableRefund = transactionService.getAvailableRefund(
                refundDTO.getPaymentIntent());
        if (availableRefund.compareTo(refundDTO.getAmount()) < 0) {
            throw new IllegalArgumentException("Refund amount exceed available refund amount");
        }

        String status = transactionService.createRefund(refundDTO.getPaymentIntent(),
                                                        refundDTO.getAmount(),
                                                        refundDTO.getReason()
        );




        Map<String, Object> map = new HashMap<>();

        if (availableRefund.compareTo(refundDTO.getAmount()) == 0) {
            map.put("refunded", true);
        } else {
            map.put("refunded", false);
        }

        map.put("status", status);

        return ResponseEntity.ok(map);
    }

    @GetMapping("/refunds/available-refund")
    public ResponseEntity<Map<String, BigDecimal>> getAvailableRefund(@RequestParam("id") Integer id)
            throws TransactionNotFoundException {
        BigDecimal availableRefund = transactionService.getAvailableRefund(id);
        Map<String, BigDecimal> map = new HashMap<>();
        map.put("availableRefund", availableRefund);

        return ResponseEntity.ok(map);
    }

    @GetMapping("/refunds/check-eligibility")
    public ResponseEntity<Map<String, Object>> requestRefund(@RequestParam("id") Integer id,
                                                 @RequestParam("amount") String amount)
            throws TransactionNotFoundException {
        BigDecimal availableRefund = transactionService.getAvailableRefund(id);

        Map<String, Object> map = new HashMap<>();
        if(availableRefund.compareTo(new BigDecimal(amount)) < 0 ) {
            map.put("approve", Boolean.FALSE);
            return ResponseEntity.ok(map);
        } else {
            map.put("approve", Boolean.TRUE);
            map.put("availableRefund", availableRefund);
            return ResponseEntity.ok(map);
        }
    }

}

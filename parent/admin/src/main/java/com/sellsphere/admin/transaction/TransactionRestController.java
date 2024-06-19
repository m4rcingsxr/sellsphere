package com.sellsphere.admin.transaction;

import com.sellsphere.common.entity.Refund;
import com.sellsphere.common.entity.TransactionNotFoundException;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/transactions")
public class TransactionRestController {

    private final TransactionService transactionService;

    @PostMapping("/refund")
    public ResponseEntity<RefundDTO> requestRefund(@RequestBody RefundDTO refundDTO)
            throws StripeException, TransactionNotFoundException {

        // validate if refund exceed total amount (server + client - rest api call)

        Refund refund = transactionService.createRefund(refundDTO.getPaymentIntent(),
                                                        refundDTO.getAmount(),
                                                        refundDTO.getReason()
        );

        return ResponseEntity.ok(RefundDTO.builder().refundStatus(refund.getStatus()).build());
    }

}

package com.sellsphere.admin.transaction;

import com.sellsphere.common.entity.MoneyUtil;
import com.sellsphere.common.entity.Refund;
import com.sellsphere.common.entity.TransactionNotFoundException;
import com.sellsphere.common.entity.payload.RefundDTO;
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

    /**
     * Handles the refund request for a given payment intent. It checks if the refund amount
     * is within the available refund limit and processes the refund.
     *
     * @param refundDTO The refund details provided by the client.
     * @return A ResponseEntity containing the refund status and the result of the operation.
     */
    @PostMapping("/refund")
    public ResponseEntity<Map<String, Object>> requestRefund(@RequestBody RefundDTO refundDTO)
            throws StripeException, TransactionNotFoundException {

        BigDecimal availableRefund = transactionService.getAvailableRefund(refundDTO.getPaymentIntent());
        validateRefundAmount(availableRefund, refundDTO.getAmount());

        Refund refund = transactionService.createRefund(
                refundDTO.getPaymentIntent(),
                refundDTO.getAmount(),
                refundDTO.getReason()
        );

        return ResponseEntity.ok(createRefundResponse(availableRefund, refundDTO.getAmount(), refund));
    }

    /**
     * Retrieves the available refund amount for a given transaction.
     *
     * @param id The transaction ID.
     * @return A ResponseEntity containing the available refund amount.
     */
    @GetMapping("/refunds/available")
    public ResponseEntity<Map<String, BigDecimal>> getAvailableRefund(@RequestParam("id") Integer id)
            throws TransactionNotFoundException {

        BigDecimal availableRefund = transactionService.getAvailableRefund(id);
        Map<String, BigDecimal> response = Map.of("availableRefund", availableRefund);

        return ResponseEntity.ok(response);
    }

    /**
     * Checks the refund eligibility for a given transaction and amount.
     *
     * @param id     The transaction ID.
     * @param amount The amount to refund.
     * @return A ResponseEntity indicating whether the refund is approved.
     */
    @GetMapping("/refunds/eligibility")
    public ResponseEntity<Map<String, Object>> checkRefundEligibility(
            @RequestParam("id") Integer id,
            @RequestParam("amount") String amount
    ) throws TransactionNotFoundException {

        BigDecimal availableRefund = transactionService.getAvailableRefund(id);
        Map<String, Object> response = createEligibilityResponse(availableRefund, new BigDecimal(amount));

        return ResponseEntity.ok(response);
    }

    /**
     * Validates if the requested refund amount exceeds the available refund amount.
     *
     * @param availableRefund The available refund amount.
     * @param requestedAmount The requested refund amount.
     */
    private void validateRefundAmount(BigDecimal availableRefund, BigDecimal requestedAmount) {
        if (availableRefund.compareTo(requestedAmount) < 0) {
            throw new IllegalArgumentException("Refund amount exceeds available refund amount.");
        }
    }

    /**
     * Creates a response map for refund processing.
     *
     * @param availableRefund The available refund amount.
     * @param requestedAmount The requested refund amount.
     * @param refund          The refund object containing payment intent and status information.
     * @return A map containing the refund result and status.
     */
    private Map<String, Object> createRefundResponse(BigDecimal availableRefund, BigDecimal requestedAmount, Refund refund) {
        Map<String, Object> response = new HashMap<>();

        response.put("refunded", isFullRefund(availableRefund, requestedAmount));
        response.put("status", determineRefundStatus(refund, requestedAmount));
        response.put("refundedAmount", formatRefundedAmount(refund, requestedAmount));

        return response;
    }

    /**
     * Determines whether the refund is a full refund.
     *
     * @param availableRefund  The available refund amount.
     * @param requestedAmount  The requested refund amount.
     * @return True if the refund is full, false otherwise.
     */
    private boolean isFullRefund(BigDecimal availableRefund, BigDecimal requestedAmount) {
        return availableRefund.compareTo(requestedAmount) == 0;
    }

    /**
     * Determines the refund status based on the refund details and requested amount.
     *
     * @param refund          The refund object.
     * @param requestedAmount The requested refund amount.
     * @return The refund status as a String.
     */
    private String determineRefundStatus(Refund refund, BigDecimal requestedAmount) {
        BigDecimal totalRefunded = refund.getPaymentIntent().getTotalRefunded().add(refund.getDisplayAmount());
        BigDecimal totalAmount = refund.getPaymentIntent().getDisplayAmount();

        if ("failed".equals(refund.getStatus())) {
            return "failed";
        }

        // Use compareTo() for value comparison without considering scale
        return totalRefunded.compareTo(totalAmount) == 0 ? "refunded" : "Partial Refund";
    }
    /**
     * Formats the refunded amount to include the newly refunded amount.
     *
     * @param refund          The refund object.
     * @param requestedAmount The requested refund amount.
     * @return The formatted refunded amount as a String.
     */
    private String formatRefundedAmount(Refund refund, BigDecimal requestedAmount) {
        BigDecimal totalRefundedAmount = refund.getPaymentIntent().getTotalRefunded().add(requestedAmount);
        String currencyCode = refund.getPaymentIntent().getTargetCurrency().getCode();

        return MoneyUtil.formatAmount(totalRefundedAmount, currencyCode);
    }

    /**
     * Creates a response map for refund eligibility check.
     *
     * @param availableRefund The available refund amount.
     * @param requestedAmount The requested refund amount.
     * @return A map containing the refund eligibility result.
     */
    private Map<String, Object> createEligibilityResponse(BigDecimal availableRefund, BigDecimal requestedAmount) {
        Map<String, Object> response = new HashMap<>();
        response.put("approve", availableRefund.compareTo(requestedAmount) >= 0);
        response.put("availableRefund", availableRefund);
        return response;
    }

}

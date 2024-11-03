package com.sellsphere.admin.transaction;

import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.common.entity.MoneyUtil;
import com.sellsphere.common.entity.PaymentIntent;
import com.sellsphere.common.entity.TransactionNotFoundException;
import com.sellsphere.payment.checkout.StripeCheckoutService;
import com.stripe.exception.StripeException;
import com.stripe.model.Refund;
import com.stripe.param.RefundCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private static final int TRANSACTION_PER_PAGE = 10;
    private final PaymentIntentRepository transactionRepository;
    private final StripeCheckoutService stripeCheckoutService;
    private final RefundRepository refundRepository;

    /**
     * Lists transactions with paging and sorting.
     *
     * @param helper The helper for paging and sorting.
     * @param pageNum The page number to retrieve.
     */
    public void listEntities(PagingAndSortingHelper helper, Integer pageNum) {
        helper.listEntities(pageNum, TRANSACTION_PER_PAGE, transactionRepository);
    }

    /**
     * Creates a refund for the specified payment intent and amount.
     *
     * @param paymentIntentId The ID of the payment intent.
     * @param amount          The refund amount.
     * @param reason          The reason for the refund.
     * @return platform Refund.
     */
    public com.sellsphere.common.entity.Refund createRefund(Integer paymentIntentId, BigDecimal amount, String reason)
            throws TransactionNotFoundException, StripeException {

        PaymentIntent paymentIntent = findTransactionById(paymentIntentId);

        long stripeAmount = MoneyUtil.convertToStripeAmount(amount, paymentIntent.getTargetCurrency().getUnitAmount());

        Refund stripeRefund = stripeCheckoutService.createRefund(
                paymentIntent.getStripeId(),
                stripeAmount,
                RefundCreateParams.Reason.valueOf(reason)
        );

        return saveRefund(stripeRefund, paymentIntent);
    }

    /**
     * Retrieves the available refund amount for the given transaction.
     *
     * @param transactionId The ID of the transaction.
     * @return The available refund amount.
     */
    public BigDecimal getAvailableRefund(Integer transactionId) throws TransactionNotFoundException {
        PaymentIntent transaction = findTransactionById(transactionId);

        Long amountRefunded = transaction.getCharge().getAmountRefunded();
        Long amount = transaction.getCharge().getAmount();

        return MoneyUtil.convertToDisplayPrice(amount - amountRefunded, transaction.getTargetCurrency().getUnitAmount());
    }

    /**
     * Finds a PaymentIntent by ID, throwing an exception if not found.
     *
     * @param id The ID of the PaymentIntent.
     * @return The found PaymentIntent.
     */
    public PaymentIntent findTransactionById(Integer id) throws TransactionNotFoundException {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found with ID: " + id));
    }

    /**
     * Saves a refund entity after it's created by Stripe.
     *
     * @param stripeRefund   The refund created by Stripe.
     * @param paymentIntent  The payment intent associated with the refund.
     * @return The saved Refund entity.
     */
    private com.sellsphere.common.entity.Refund saveRefund(Refund stripeRefund, PaymentIntent paymentIntent) {
        return refundRepository.save(
                com.sellsphere.common.entity.Refund.builder()
                        .stripeId(stripeRefund.getId())
                        .created(stripeRefund.getCreated())
                        .amount(stripeRefund.getAmount())
                        .reason(stripeRefund.getReason())
                        .status(stripeRefund.getStatus())
                        .paymentIntent(paymentIntent)
                        .charge(paymentIntent.getCharge())
                        .currency(paymentIntent.getTargetCurrency())
                        .created(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                        .build()
        );
    }

    public List<PaymentIntent> findAll() {
        return transactionRepository.findAll(Sort.by(Sort.Direction.ASC, "created"));
    }
}

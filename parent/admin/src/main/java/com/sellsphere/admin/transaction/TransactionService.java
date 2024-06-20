package com.sellsphere.admin.transaction;

import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.common.entity.PaymentIntent;
import com.sellsphere.common.entity.TransactionNotFoundException;
import com.sellsphere.payment.StripeCheckoutService;
import com.stripe.exception.StripeException;
import com.stripe.model.Refund;
import com.stripe.param.RefundCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private static final int TRANSACTION_PER_PAGE = 10;
    private final PaymentIntentRepository transactionRepository;
    private final StripeCheckoutService stripeCheckoutService;
    private final RefundRepository refundRepository;
    private final PaymentIntentRepository paymentIntentRepository;
    private final BalanceTransactionRepository balanceTransactionRepository;


    public void listEntities(PagingAndSortingHelper helper, Integer pageNum) {
        helper.listEntities(pageNum, TRANSACTION_PER_PAGE, transactionRepository);
    }

    public String createRefund(Integer paymentIntentId, BigDecimal amount, String reason)
            throws TransactionNotFoundException, StripeException {
        PaymentIntent paymentIntent = transactionRepository
                .findById(paymentIntentId)
                .orElseThrow(TransactionNotFoundException::new);


        Refund stripeRefund = stripeCheckoutService.createRefund(
                paymentIntent.getStripeId(),
                amount.multiply(
                        paymentIntent.getCurrency().getUnitAmount()).setScale(2,
                                                                              RoundingMode.CEILING
                ).longValue(),
                RefundCreateParams.Reason.valueOf(reason)
        );

        var intent = paymentIntentRepository
                .findByStripeId(stripeRefund.getPaymentIntent())
                .orElseThrow(TransactionNotFoundException::new);

        return refundRepository.save(
                com.sellsphere.common.entity.Refund.builder()
                        .stripeId(stripeRefund.getId())
                        .created(stripeRefund.getCreated())
                        .amount(stripeRefund.getAmount())
                        .reason(stripeRefund.getReason())
                        .status(stripeRefund.getStatus())
                        .paymentIntent(intent)
                        .charge(intent.getCharge())
                        .currency(intent.getCurrency())
                        .created(LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(1)))
                        .build()
        ).getStatus();
    }


    public PaymentIntent findById(Integer id) throws TransactionNotFoundException {
        return transactionRepository.findById(id).orElseThrow(TransactionNotFoundException::new);
    }
}


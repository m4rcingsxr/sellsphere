package com.sellsphere.admin.transaction;

import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.common.entity.Currency;
import com.sellsphere.common.entity.PaymentIntent;
import com.sellsphere.common.entity.Refund;
import com.sellsphere.common.entity.TransactionNotFoundException;
import com.sellsphere.payment.StripeCheckoutService;
import com.stripe.exception.StripeException;
import com.stripe.param.RefundCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private static final int TRANSACTION_PER_PAGE = 10;
    private final PaymentIntentRepository transactionRepository;
    private final StripeCheckoutService stripeCheckoutService;
    private final RefundRepository refundRepository;
    private final PaymentIntentRepository paymentIntentRepository;


    public void listEntities(PagingAndSortingHelper helper, Integer pageNum) {
        helper.listEntities(pageNum, TRANSACTION_PER_PAGE, transactionRepository);
    }

    public Refund createRefund(Integer paymentIntentId, BigDecimal amount, String reason)
            throws TransactionNotFoundException, StripeException {
        PaymentIntent paymentIntent = transactionRepository
                .findById(paymentIntentId)
                .orElseThrow(TransactionNotFoundException::new);


        com.stripe.model.Refund stripeRefund = stripeCheckoutService.createRefund(
                paymentIntent.getStripeId(), amount.multiply(BigDecimal.valueOf(paymentIntent.getCurrency().getUnitAmount())).setScale(2, RoundingMode.CEILING).longValue(),
                RefundCreateParams.Reason.valueOf(reason)
        );


        return refundRepository.save(
                Refund.builder()
                        .stripeId(stripeRefund.getId())
                        .created(stripeRefund.getCreated())
                        .amount(stripeRefund.getAmount())
                        .reason(stripeRefund.getReason())
                        .status(stripeRefund.getStatus())
                        .paymentIntent(paymentIntent)
                        .currency(paymentIntent.getCurrency())
                        .created(LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(1)))
                        .build()
        );
    }


    public PaymentIntent findById(Integer id) throws TransactionNotFoundException {
        return transactionRepository.findById(id).orElseThrow(TransactionNotFoundException::new);
    }
}


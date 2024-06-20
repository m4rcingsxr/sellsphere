package com.sellsphere.client.checkout;

import com.sellsphere.client.setting.CurrencyRepository;
import com.sellsphere.client.setting.SettingService;
import com.sellsphere.client.shoppingcart.CartItemRepository;
import com.sellsphere.common.entity.*;
import com.sellsphere.common.entity.payload.PaymentRequestDTO;
import com.sellsphere.payment.StripeCheckoutService;
import com.stripe.exception.StripeException;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentIntentUpdateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final PaymentIntentRepository repository;
    private final CurrencyRepository currencyRepository;
    private final StripeCheckoutService stripeService;
    private final PaymentIntentRepository paymentIntentRepository;
    private final CartItemRepository cartItemRepository;
    private final SettingService settingService;

    public String savePaymentIntent(PaymentRequestDTO request, Customer customer)
            throws TransactionNotFoundException, StripeException, CurrencyNotFoundException {

        PaymentIntent transaction = findIncompleteTransaction(customer)
                .orElseThrow(TransactionNotFoundException::new);

        var paymentIntent = stripeService.updatePaymentIntent(transaction.getStripeId(), request);

        Currency currency = currencyRepository.findByCode(request.getCurrencyCode()).orElseThrow(CurrencyNotFoundException::new);

        // update transaction
        transaction.setAmount(request.getAmountTotal());
        transaction.setCurrency(currency);

        return paymentIntent.getClientSecret();
    }

    public String initializePaymentIntent(Customer customer)
            throws StripeException, CurrencyNotFoundException {
        Optional<PaymentIntent> transaction = findIncompleteTransaction(customer);
        Currency currency = settingService.getCurrency();

        BigDecimal unitAmount = currency.getUnitAmount();

        List<CartItem> cart = cartItemRepository.findByCustomer(customer);
        if (cart.isEmpty()) throw new IllegalStateException("Cart should have items.");


        BigDecimal amount = BigDecimal.ZERO;
        for (CartItem cartItem : cart) {
            BigDecimal productSubtotal = BigDecimal.valueOf(cartItem.getQuantity())
                    .multiply(BigDecimal.valueOf(cartItem.getProduct().getId()))
                    .multiply(unitAmount);

            amount = amount.add(productSubtotal);
        }

        if (transaction.isPresent()) {
            PaymentIntent paymentIntent = transaction.get();

            // update platform entity
            paymentIntent.setAmount(amount.longValue());
            paymentIntent.setCurrency(currency);

            return stripeService.updatePaymentIntent(
                    paymentIntent.getStripeId(),
                    PaymentRequestDTO.builder()
                            .amountTotal(amount.longValue())
                            .currencyCode(currency.getCode())
                            .build()
            ).getClientSecret();
        } else {

            // platform entity persisted by payment_intent.created webhook
            var stripePaymentIntent = stripeService.createPaymentIntent(
                    PaymentIntentCreateParams.builder()
                            .setCustomer(customer.getStripeId())
                            .setAmount(amount.longValue())
                            .setSetupFutureUsage(
                                    PaymentIntentCreateParams.SetupFutureUsage.ON_SESSION)
                            .setCurrency(currency.getCode())
                            .setAutomaticPaymentMethods(
                                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(
                                            true).build()
                            )
                            .build()
            );

            return stripePaymentIntent.getClientSecret();
        }
    }

    public Optional<PaymentIntent> findIncompleteTransaction(Customer customer) {
        return repository.findByCustomerAndStatus(customer, "requires_payment_method");
    }

    public PaymentIntent save(PaymentIntent tr) {
        return repository.save(tr);
    }
}

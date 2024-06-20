package com.sellsphere.client.checkout;

import com.sellsphere.client.setting.CurrencyRepository;
import com.sellsphere.client.setting.SettingService;
import com.sellsphere.client.shoppingcart.CartItemRepository;
import com.sellsphere.common.entity.*;
import com.sellsphere.common.entity.payload.PaymentRequestDTO;
import com.sellsphere.payment.StripeCheckoutService;
import com.stripe.exception.StripeException;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final CurrencyRepository currencyRepository;
    private final CartItemRepository cartItemRepository;
    private final StripeCheckoutService stripeService;
    private final PaymentIntentRepository repository;
    private final SettingService settingService;

    public String savePaymentIntent(PaymentRequestDTO request, Customer customer)
            throws TransactionNotFoundException, StripeException, CurrencyNotFoundException {

        PaymentIntent transaction = findIncompleteTransaction(customer)
                .orElseThrow(TransactionNotFoundException::new);

        var paymentIntent = stripeService.updatePaymentIntent(transaction.getStripeId(), request);
        Currency currency = currencyRepository.findByCode(request.getCurrencyCode()).orElseThrow(CurrencyNotFoundException::new);

        transaction.setAmount(request.getAmountTotal());
        transaction.setCurrency(currency);

        return paymentIntent.getClientSecret();
    }

    public String initializePaymentIntent(Customer customer)
            throws CurrencyNotFoundException, StripeException {
        Optional<PaymentIntent> transaction = findIncompleteTransaction(customer);
        Currency currency = settingService.getCurrency();
        BigDecimal unitAmount = currency.getUnitAmount();
        List<CartItem> cart = cartItemRepository.findByCustomer(customer);

        if (cart.isEmpty()) throw new IllegalStateException("Cart should have items.");

        BigDecimal amount = calculateCartTotal(cart, unitAmount);

        if (transaction.isPresent()) {
            PaymentIntent paymentIntent = transaction.get();
            return updateExistingPaymentIntent(paymentIntent, currency, amount);
        } else {
            return createNewPaymentIntent(customer, currency, amount);
        }
    }

    private BigDecimal calculateCartTotal(List<CartItem> cart, BigDecimal unitAmount) {
        BigDecimal amount = BigDecimal.ZERO;
        for (CartItem cartItem : cart) {
            BigDecimal productSubtotal = BigDecimal.valueOf(cartItem.getQuantity())
                    .multiply(cartItem.getProduct().getDiscountPrice())
                    .multiply(unitAmount);
            amount = amount.add(productSubtotal);
        }
        return amount;
    }

    private String updateExistingPaymentIntent(PaymentIntent paymentIntent, Currency currency, BigDecimal amount)
            throws StripeException {

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
    }

    private String createNewPaymentIntent(Customer customer, Currency currency, BigDecimal amount)
            throws StripeException {
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

    public Optional<PaymentIntent> findIncompleteTransaction(Customer customer) {
        return repository.findByCustomerAndStatus(customer, "requires_payment_method");
    }

    public PaymentIntent save(PaymentIntent tr) {
        return repository.save(tr);
    }
}

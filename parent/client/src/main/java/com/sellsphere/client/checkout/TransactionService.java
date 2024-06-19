package com.sellsphere.client.checkout;

import com.sellsphere.client.setting.CurrencyRepository;
import com.sellsphere.common.entity.*;
import com.sellsphere.payment.StripeCheckoutService;
import com.sellsphere.payment.payload.PaymentRequest;
import com.stripe.exception.StripeException;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final PaymentIntentRepository repository;
    private final CurrencyRepository currencyRepository;
    private final StripeCheckoutService stripeService;
    private final PaymentIntentRepository paymentIntentRepository;

    public Optional<PaymentIntent> findIncompleteTransaction(Customer customer) {
        return repository.findByCustomerAndStatus(customer, "requires_payment_method");
    }

    public String savePaymentIntent(PaymentRequest request, Customer customer)
            throws StripeException, CurrencyNotFoundException {

        Optional<PaymentIntent> transaction = findIncompleteTransaction(customer);
        com.stripe.model.PaymentIntent stripePaymentIntent;

        if(transaction.isPresent()) {
            stripePaymentIntent = stripeService.updatePaymentIntent(transaction.get().getStripeId(), request);

            // and save it in service
            PaymentIntent paymentIntent = transaction.get();
            if(!paymentIntent.getCurrency().getCode().equalsIgnoreCase(
                    stripePaymentIntent.getCurrency())) {
                Currency currency = currencyRepository.findByCode(stripePaymentIntent.getCurrency()).orElseThrow(CurrencyNotFoundException::new);
                paymentIntent.setCurrency(currency);
            }

            paymentIntent.setAmount(stripePaymentIntent.getAmount());
            paymentIntentRepository.save(paymentIntent);
        } else {
            var address = request.getCustomerDetails().getAddress();

            stripePaymentIntent = stripeService.createPaymentIntent(
                    PaymentIntentCreateParams.Shipping.builder()
                            .setPhone(request.getPhoneNumber())
                            .setName(request.getMetadata().get("recipient_name"))
                            .setAddress(PaymentIntentCreateParams.Shipping.Address.builder()
                                                .setCity(address.getCity())
                                                .setState(address.getState())
                                                .setLine1(address.getLine1())
                                                .setLine2(address.getLine2())
                                                .setCountry(address.getCountry())
                                                .setPostalCode(address.getPostalCode())
                                                .build()).build(),
                    request.getAmountTotal(),
                    request.getCurrencyCode(),
                    request.getMetadata().get("courier_id"),
                    request.getMetadata().get("email"),
                    request.getMetadata().get("addressIdx"),
                    request.getCalculationId(),
                    customer.getStripeId());
        }


        return stripePaymentIntent.getClientSecret();
    }

    public PaymentIntent save(PaymentIntent tr) {
        return repository.save(tr);
    }
}

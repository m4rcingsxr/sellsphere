package com.sellsphere.client.paymentmethod;

import com.google.inject.Guice;
import com.sellsphere.client.webhook.CardRepository;
import com.sellsphere.client.webhook.PaymentMethodRepository;
import com.sellsphere.common.entity.Card;
import com.sellsphere.common.entity.Customer;
import com.sellsphere.payment.StripeModule;
import com.sellsphere.payment.paymentsource.StripePaymentMethodService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentMethod;
import com.stripe.model.SetupIntent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PaymentMethodService {

    private final CardRepository cardRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final StripePaymentMethodService stripePaymentMethodService;

    @Autowired
    public PaymentMethodService(CardRepository cardRepository, PaymentMethodRepository paymentMethodRepository) {
        this.stripePaymentMethodService = Guice.createInjector(new StripeModule()).getInstance(
                StripePaymentMethodService.class);
        this.cardRepository = cardRepository;
        this.paymentMethodRepository = paymentMethodRepository;

    }


    public List<Card> listAllCardsByCustomer(Customer customer) {
        return cardRepository.findAllByCustomer(customer);
    }

    @Transactional
    public void attachPaymentMethod(Customer customer, String stripePaymentMethodId) throws StripeException {
        PaymentMethod paymentMethod = stripePaymentMethodService.attachPaymentMethod(customer.getStripeId(),
                                                                                     stripePaymentMethodId
        );

        paymentMethodRepository.save(
                com.sellsphere.common.entity.PaymentMethod.builder()
                        .type(paymentMethod.getType())
                        .stripeId(paymentMethod.getId())
                        .customer(customer)
                        .build()
        );

    }

    public void detachPaymentMethod(String stripePaymentMethodId) throws StripeException {
        stripePaymentMethodService.detachPaymentMethod(stripePaymentMethodId);
    }


    public SetupIntent getSetupIntent(Customer customer) throws StripeException {
        return stripePaymentMethodService.createSetupIntent(customer.getStripeId());
    }
}

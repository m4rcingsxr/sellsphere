package com.sellsphere.client.checkout;

import com.sellsphere.client.address.AddressService;
import com.sellsphere.client.setting.CountryRepository;
import com.sellsphere.client.setting.CurrencyRepository;
import com.sellsphere.client.setting.SettingService;
import com.sellsphere.client.shoppingcart.CartItemRepository;
import com.sellsphere.common.entity.*;
import com.sellsphere.common.entity.payload.PaymentRequestDTO;
import com.sellsphere.payment.checkout.StripeCheckoutService;
import com.stripe.exception.StripeException;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final StripeCheckoutService stripeService;
    private final SettingService settingService;
    private final AddressService addressService;

    private final CurrencyRepository currencyRepository;
    private final PaymentIntentRepository repository;
    private final CountryRepository countryRepository;
    private final CourierRepository courierRepository;


    /**
     * Retrieves a PaymentIntent by its Stripe ID.
     *
     * @param paymentIntentId the Stripe ID of the PaymentIntent
     * @return the PaymentIntent
     * @throws TransactionNotFoundException if the PaymentIntent is not found
     */
    public PaymentIntent getByStripeId(String paymentIntentId) throws TransactionNotFoundException {
        return repository.findByStripeId(paymentIntentId)
                .orElseThrow(TransactionNotFoundException::new);
    }

    /**
     * Saves a new PaymentIntent based on the provided payment request and customer.
     *
     * @param request  the payment request details
     * @param customer the customer making the payment
     * @return the client secret of the created PaymentIntent
     * @throws StripeException           if there is an error with Stripe operations
     * @throws CurrencyNotFoundException if the specified currency is not found
     * @throws CountryNotFoundException  if the specified country is not found
     * @throws AddressNotFoundException  if the specified address is not found
     */
    public String savePaymentIntent(PaymentRequestDTO request, Customer customer)
            throws StripeException, CurrencyNotFoundException,
            CountryNotFoundException, AddressNotFoundException {

        PaymentIntentCreateParams params = buildPaymentIntentParams(request, customer);
        com.stripe.model.PaymentIntent paymentIntent = stripeService.createPaymentIntent(params);

        Currency targetCurrency = currencyRepository.findByCode(request.getCurrencyCode())
                .orElseThrow(CurrencyNotFoundException::new);
        Currency baseCurrency = settingService.getCurrency();
        Country country = countryRepository.findByCode(request.getAddress().getCountryCode())
                .orElseThrow(CountryNotFoundException::new);

        Address address = getAddress(request, customer, country);

        PaymentIntent transaction = buildPaymentIntent(request, customer, paymentIntent,
                                                       targetCurrency, baseCurrency, address);

        Courier courier = getOrCreateCourier(request);

        transaction.setCourier(courier);
        save(transaction);
        return paymentIntent.getClientSecret();
    }

    /**
     * Builds the PaymentIntentCreateParams for creating a new PaymentIntent.
     *
     * @param request  the payment request details
     * @param customer the customer making the payment
     * @return the PaymentIntentCreateParams
     */
    private PaymentIntentCreateParams buildPaymentIntentParams(PaymentRequestDTO request, Customer customer) {
        return PaymentIntentCreateParams.builder()
                .setCustomer(customer.getStripeId())
                .setAmount(request.getAmountTotal())
                .setCurrency(request.getCurrencyCode())
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build()
                )
                .build();
    }

    /**
     * Retrieves or creates an Address based on the payment request.
     *
     * @param request  the payment request details
     * @param customer the customer making the payment
     * @param country  the country of the address
     * @return the Address
     * @throws AddressNotFoundException if the address is not found
     */
    private Address getAddress(PaymentRequestDTO request, Customer customer, Country country)
            throws AddressNotFoundException {
        if (request.getAddress().getId() != null) {
            return addressService.getById(request.getAddress().getId());
        } else {
            String[] fullName = request.getAddress().getFullName().split(" ");
            return addressService.save(Address.builder()
                                               .addressLine1(request.getAddress().getAddressLine1())
                                               .addressLine2(request.getAddress().getAddressLine2())
                                               .city(request.getAddress().getCity())
                                               .country(country)
                                               .customer(customer)
                                               .firstName(fullName[0])
                                               .lastName(fullName[1])
                                               .state(request.getAddress().getState())
                                               .primary(false)
                                               .postalCode(request.getAddress().getPostalCode())
                                               .phoneNumber(request.getAddress().getPhoneNumber())
                                               .build());
        }
    }

    /**
     * Builds a PaymentIntent entity from the provided details.
     *
     * @param request       the payment request details
     * @param customer      the customer making the payment
     * @param paymentIntent the Stripe PaymentIntent
     * @param targetCurrency the target currency
     * @param baseCurrency  the base currency
     * @param address       the shipping address
     * @return the PaymentIntent entity
     */
    private PaymentIntent buildPaymentIntent(PaymentRequestDTO request, Customer customer,
                                             com.stripe.model.PaymentIntent paymentIntent,
                                             Currency targetCurrency, Currency baseCurrency,
                                             Address address) {
        return PaymentIntent.builder()
                .shippingAddress(address)
                .amount(request.getAmountTotal())
                .stripeId(paymentIntent.getId())
                .shippingAmount(request.getShippingAmount())
                .exchangeRate(request.getExchangeRate())
                .taxAmount(request.getAmountTax())
                .shippingTax(request.getShippingTax())
                .targetCurrency(targetCurrency)
                .baseCurrency(baseCurrency)
                .customer(customer)
                .created(Instant.now().toEpochMilli())
                .status(paymentIntent.getStatus())
                .build();
    }

    /**
     * Retrieves or creates a Courier based on the payment request.
     *
     * @param request the payment request details
     * @return the Courier
     */
    private Courier getOrCreateCourier(PaymentRequestDTO request) {
        return courierRepository.findByName(request.getCourierName())
                .orElseGet(() -> courierRepository.save(Courier.builder()
                                                                .name(request.getCourierName())
                                                                .maxDeliveryTime(request.getMaxDeliveryTime())
                                                                .minDeliveryTime(request.getMinDeliveryTime())
                                                                .logoUrl(request.getCourierLogoUrl())
                                                                .build()));
    }

    /**
     * Saves the PaymentIntent entity.
     *
     * @param transaction the PaymentIntent entity
     * @return the saved PaymentIntent entity
     */
    public PaymentIntent save(PaymentIntent transaction) {
        return repository.save(transaction);
    }
}

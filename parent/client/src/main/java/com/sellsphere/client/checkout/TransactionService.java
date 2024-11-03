package com.sellsphere.client.checkout;

import com.sellsphere.client.address.AddressService;
import com.sellsphere.client.order.OrderService;
import com.sellsphere.client.setting.CountryService;
import com.sellsphere.client.setting.SettingService;
import com.sellsphere.common.entity.*;
import com.sellsphere.common.entity.payload.PaymentRequestDTO;
import com.sellsphere.payment.checkout.StripeCheckoutService;
import com.stripe.exception.StripeException;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final StripeCheckoutService stripeService;
    private final SettingService settingService;
    private final AddressService addressService;
    private final CurrencyService currencyService;
    private final CountryService countryService;
    private final PaymentIntentRepository repository;
    private final CourierRepository courierRepository;
    private final OrderService orderService;

    /**
     * Retrieves a PaymentIntent by its Stripe ID. Throws an exception if not found.
     *
     * @param paymentIntentId The Stripe ID of the PaymentIntent.
     * @return The PaymentIntent entity.
     * @throws TransactionNotFoundException If the PaymentIntent is not found in the database.
     */
    public PaymentIntent getByStripeId(String paymentIntentId) throws TransactionNotFoundException {
        return repository.findByStripeId(paymentIntentId)
                .orElseThrow(TransactionNotFoundException::new);
    }

    /**
     * Creates a new PaymentIntent on Stripe based on the provided payment details, customer information,
     * and saves it in the database.
     *
     * @param request  DTO containing payment details like amount, shipping, and tax.
     * @param customer The customer making the payment.
     * @return The client secret for the PaymentIntent, used to confirm the payment on the client side.
     * @throws StripeException           If there is an issue creating the PaymentIntent on Stripe.
     * @throws CurrencyNotFoundException If the requested currency is not found.
     * @throws CountryNotFoundException  If the specified country is not found.
     * @throws AddressNotFoundException  If the address provided is invalid or not found.
     */
    public String savePaymentIntent(PaymentRequestDTO request, Customer customer)
            throws StripeException, CurrencyNotFoundException,
            CountryNotFoundException, AddressNotFoundException {

        // Retrieve the base currency from settings
        Currency baseCurrency = settingService.getCurrency();

        // Build the Stripe PaymentIntent creation parameters
        PaymentIntentCreateParams params = buildPaymentIntentParams(request, customer, baseCurrency);

        // Create the PaymentIntent on Stripe
        com.stripe.model.PaymentIntent paymentIntent = stripeService.createPaymentIntent(params);

        // Retrieve or create the shipping address for the customer
        Country country = countryService.getCountryByCode(request.getAddress().getCountryCode());
        Address address = getAddress(request, customer, country);

        // Build and save the PaymentIntent entity in the local database
        PaymentIntent transaction = buildPaymentIntent(request, customer, paymentIntent, baseCurrency, address);
        Courier courier = getOrCreateCourier(request);  // Retrieve or create a courier for the shipping
        transaction.setCourier(courier);

        PaymentIntent platformTransaction = save(transaction);// Persist the PaymentIntent entity to the database
        orderService.createOrder(platformTransaction);

        return paymentIntent.getClientSecret();  // Return the client secret to be used by the client
    }

    /**
     * Builds the Stripe PaymentIntent parameters with the amount, customer, and automatic payment methods enabled.
     *
     * @param request       DTO containing payment details.
     * @param customer      The customer making the payment.
     * @param baseCurrency  The currency to use for the transaction.
     * @return A configured PaymentIntentCreateParams object ready to be sent to Stripe.
     */
    private PaymentIntentCreateParams buildPaymentIntentParams(PaymentRequestDTO request, Customer customer, Currency baseCurrency) {
        return PaymentIntentCreateParams.builder()
                .setCustomer(customer.getStripeId()) // Set the Stripe customer ID
                .setAmount(request.getAmountTotal()) // Set the total payment amount
                .setCurrency(baseCurrency.getCode()) // Set the currency code (e.g., USD)
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true) // Enable automatic payment methods (e.g., cards, bank transfers)
                                .build()
                )
                .build();
    }

    /**
     * Retrieves an existing address or creates a new address for the customer based on the payment request.
     *
     * @param request  DTO containing address details.
     * @param customer The customer associated with the address.
     * @param country  The country for the address.
     * @return The Address entity.
     * @throws AddressNotFoundException If the address is not found.
     */
    private Address getAddress(PaymentRequestDTO request, Customer customer, Country country)
            throws AddressNotFoundException {

        // Check if the address ID exists, retrieve it, otherwise create a new one
        if (request.getAddress().getId() != null) {
            return addressService.getById(request.getAddress().getId());
        } else {
            // Split the full name into first and last name
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
     * Builds a PaymentIntent entity to be stored in the local database.
     *
     * @param request       DTO containing payment details.
     * @param customer      The customer making the payment.
     * @param paymentIntent The PaymentIntent object created on Stripe.
     * @param baseCurrency  The currency of the transaction.
     * @param address       The shipping address for the transaction.
     * @return A PaymentIntent entity that is ready to be saved in the database.
     */
    private PaymentIntent buildPaymentIntent(PaymentRequestDTO request, Customer customer,
                                             com.stripe.model.PaymentIntent paymentIntent, Currency baseCurrency,
                                             Address address) {
        return PaymentIntent.builder()
                .shippingAddress(address)
                .amount(request.getAmountTotal()) // Total amount for the transaction
                .stripeId(paymentIntent.getId()) // The Stripe PaymentIntent ID
                .shippingAmount(request.getShippingAmount()) // Shipping cost for the transaction
                .taxAmount(request.getAmountTax()) // Tax applied to the transaction
                .shippingTax(request.getShippingTax()) // Tax applied to shipping
                .targetCurrency(baseCurrency) // The currency used for the payment
                .customer(customer) // The customer making the payment
                .created(Instant.now().toEpochMilli()) // Record the current time as the creation time
                .status(paymentIntent.getStatus()) // Status of the PaymentIntent on Stripe (e.g., "requires_payment_method")
                .build();
    }

    /**
     * Retrieves or creates a Courier entity based on the provided courier name in the payment request.
     *
     * @param request DTO containing courier information.
     * @return A Courier entity corresponding to the shipping provider.
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
     * Saves the PaymentIntent entity to the database.
     *
     * @param transaction The PaymentIntent entity to be saved.
     * @return The saved PaymentIntent entity.
     */
    public PaymentIntent save(PaymentIntent transaction) {
        return repository.save(transaction);
    }
}

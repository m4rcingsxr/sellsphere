package com.sellsphere.client.checkout;

import com.sellsphere.client.address.AddressService;
import com.sellsphere.client.setting.CountryService;
import com.sellsphere.client.setting.SettingService;
import com.sellsphere.common.entity.*;
import com.sellsphere.common.entity.payload.AddressDTO;
import com.sellsphere.common.entity.payload.PaymentRequestDTO;
import com.sellsphere.payment.checkout.StripeCheckoutService;
import com.stripe.exception.StripeException;
import com.stripe.param.PaymentIntentCreateParams;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private StripeCheckoutService stripeService;

    @Mock
    private SettingService settingService;

    @Mock
    private AddressService addressService;

    @Mock
    private CurrencyService currencyService;

    @Mock
    private CountryService countryService;

    @Mock
    private PaymentIntentRepository repository;

    @Mock
    private CourierRepository courierRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void givenPaymentRequestWhenExistingData_whenSaveTransaction_thenTransactionShouldBeSaved() throws StripeException, CurrencyNotFoundException, CountryNotFoundException, AddressNotFoundException {
        // Arrange
        PaymentRequestDTO request = createPaymentRequestDTO();
        request.getAddress().setId(1); // Existing address
        Customer customer = createCustomer();
        com.stripe.model.PaymentIntent stripePaymentIntent = createStripePaymentIntent();

        when(stripeService.createPaymentIntent(any(PaymentIntentCreateParams.class))).thenReturn(stripePaymentIntent);
        when(currencyService.getByCode(request.getCurrencyCode())).thenReturn(createCurrency());
        when(settingService.getCurrency()).thenReturn(createCurrency());
        when(countryService.getCountryByCode(request.getAddress().getCountryCode())).thenReturn(createCountry());
        when(addressService.getById(request.getAddress().getId())).thenReturn(createAddress());
        when(courierRepository.findByName(request.getCourierName())).thenReturn(Optional.empty());
        when(courierRepository.save(any(Courier.class))).thenReturn(createCourier());
        when(repository.save(any(PaymentIntent.class))).thenReturn(createPaymentIntent(request, customer, stripePaymentIntent));

        // Act
        String clientSecret = transactionService.savePaymentIntent(request, customer);

        // Assert
        assertNotNull(clientSecret);
        assertEquals(stripePaymentIntent.getClientSecret(), clientSecret);
        verify(addressService).getById(request.getAddress().getId());
        verify(addressService, never()).save(any(Address.class));
        verify(repository).save(any(PaymentIntent.class));
        verify(stripeService).createPaymentIntent(any(PaymentIntentCreateParams.class));
    }

    @Test
    void givenPaymentRequestWithNewAddress_whenSaveTransaction_thenTransactionAndAddressShouldBeSave() throws StripeException, CurrencyNotFoundException, CountryNotFoundException, AddressNotFoundException {
        // Arrange
        PaymentRequestDTO request = createPaymentRequestDTO();
        request.getAddress().setId(null); // New address
        Customer customer = createCustomer();
        com.stripe.model.PaymentIntent stripePaymentIntent = createStripePaymentIntent();

        when(stripeService.createPaymentIntent(any(PaymentIntentCreateParams.class))).thenReturn(stripePaymentIntent);
        when(currencyService.getByCode(request.getCurrencyCode())).thenReturn(createCurrency());
        when(settingService.getCurrency()).thenReturn(createCurrency());
        when(countryService.getCountryByCode(request.getAddress().getCountryCode())).thenReturn(createCountry());
        when(addressService.save(any(Address.class))).thenReturn(createAddress());
        when(courierRepository.findByName(request.getCourierName())).thenReturn(Optional.empty());
        when(courierRepository.save(any(Courier.class))).thenReturn(createCourier());
        when(repository.save(any(PaymentIntent.class))).thenReturn(createPaymentIntent(request, customer, stripePaymentIntent));

        // Act
        String clientSecret = transactionService.savePaymentIntent(request, customer);

        // Assert
        assertNotNull(clientSecret);
        assertEquals(stripePaymentIntent.getClientSecret(), clientSecret);
        verify(addressService, never()).getById(any(Integer.class));
        verify(addressService).save(any(Address.class));
        verify(repository).save(any(PaymentIntent.class));
        verify(stripeService).createPaymentIntent(any(PaymentIntentCreateParams.class));
    }

    // Helper methods for creating test data
    private PaymentRequestDTO createPaymentRequestDTO() {
        return PaymentRequestDTO.builder()
                .address(AddressDTO.builder()
                                 .firstName("John")
                                 .lastName("Doe")
                                 .phoneNumber("1234567890")
                                 .addressLine1("123 Main St")
                                 .city("New York")
                                 .state("NY")
                                 .countryCode("US")
                                 .postalCode("10001")
                                 .fullName("John Doe")
                                 .build())
                .currencyCode("USD")
                .courierName("FedEx")
                .courierLogoUrl("https://example.com/fedex-logo.png")
                .maxDeliveryTime(5)
                .minDeliveryTime(2)
                .exchangeRate(BigDecimal.valueOf(1.0))
                .amountTotal(10000L)
                .amountTax(800L)
                .shippingAmount(500L)
                .shippingTax(100L)
                .build();
    }

    private Customer createCustomer() {
        return Customer.builder()
                .stripeId("cus_123")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();
    }

    private com.stripe.model.PaymentIntent createStripePaymentIntent() {
        com.stripe.model.PaymentIntent paymentIntent = new com.stripe.model.PaymentIntent();
        paymentIntent.setId("pi_123");
        paymentIntent.setClientSecret("secret_123");
        return paymentIntent;
    }

    private Currency createCurrency() {
        return Currency.builder()
                .code("USD")
                .name("US Dollar")
                .symbol("$")
                .unitAmount(BigDecimal.valueOf(100))
                .build();
    }

    private Country createCountry() {
        return Country.builder()
                .code("US")
                .name("United States")
                .build();
    }

    private Address createAddress() {
        return Address.builder()
                .addressLine1("123 Main St")
                .addressLine2("Apt 4B")
                .city("New York")
                .state("NY")
                .country(createCountry())
                .postalCode("10001")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("1234567890")
                .primary(false)
                .build();
    }

    private Courier createCourier() {
        return Courier.builder()
                .name("FedEx")
                .logoUrl("https://example.com/fedex-logo.png")
                .minDeliveryTime(2)
                .maxDeliveryTime(5)
                .build();
    }

    private PaymentIntent createPaymentIntent(PaymentRequestDTO request, Customer customer, com.stripe.model.PaymentIntent stripePaymentIntent) {
        return PaymentIntent.builder()
                .shippingAddress(createAddress())
                .amount(request.getAmountTotal())
                .stripeId(stripePaymentIntent.getId())
                .shippingAmount(request.getShippingAmount())
                .exchangeRate(request.getExchangeRate())
                .taxAmount(request.getAmountTax())
                .shippingTax(request.getShippingTax())
                .targetCurrency(createCurrency())
                .baseCurrency(createCurrency())
                .customer(customer)
                .created(Instant.now().toEpochMilli())
                .status(stripePaymentIntent.getStatus())
                .build();
    }
}

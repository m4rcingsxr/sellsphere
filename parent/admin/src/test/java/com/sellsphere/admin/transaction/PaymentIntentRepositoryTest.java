package com.sellsphere.admin.transaction;

import com.sellsphere.admin.customer.AddressRepository;
import com.sellsphere.admin.customer.CustomerRepository;
import com.sellsphere.admin.order.OrderRepository;
import com.sellsphere.admin.setting.CurrencyRepository;
import com.sellsphere.common.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.jdbc.Sql;
import util.PagingTestHelper;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Sql(scripts = {"classpath:sql/payment_intents.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class PaymentIntentRepositoryTest {

    @Autowired
    private PaymentIntentRepository paymentIntentRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ChargeRepository chargeRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private CourierRepository courierRepository;

    private Customer customer;
    private Address address;
    private Courier courier;
    private Currency currency;
    @Autowired
    private CurrencyRepository currencyRepository;

    @BeforeEach
    void setup() {
        customer = customerRepository.findById(1).orElseThrow();
        address = addressRepository.findById(1).orElseThrow();
        courier = courierRepository.findById(1).orElseThrow(); // Fetch the Courier for the PaymentIntent
        currency = currencyRepository.findById(1).orElseThrow();
    }

    @Test
    void givenPaymentIntent_whenSaving_thenPersistPaymentIntent() {
        // Given
        PaymentIntent paymentIntent = createPaymentIntent();

        // When
        PaymentIntent savedPaymentIntent = paymentIntentRepository.save(paymentIntent);

        // Then
        assertNotNull(savedPaymentIntent.getId(), "PaymentIntent ID should not be null after saving.");
        assertEquals("created", savedPaymentIntent.getStatus(),
                     "PaymentIntent status should match the expected value."
        );
        assertEquals(customer.getEmail(), savedPaymentIntent.getCustomer().getEmail(),
                     "PaymentIntent customer email should match the saved value."
        );
        assertNotNull(savedPaymentIntent.getShippingAddress(), "PaymentIntent should have an associated address.");
        assertNotNull(savedPaymentIntent.getCourier(), "PaymentIntent should have an associated courier.");
    }

    @Test
    void givenPaymentIntentWithRefund_whenSaving_thenCascadePersistRefund() {
        // Given
        PaymentIntent paymentIntent = createPaymentIntent();
        Charge charge = new Charge();
        charge.setAmount(1000L);
        charge.setStripeId("charge_1");
        charge.setRefunded(false);
        charge.setAmountRefunded(100L);
        charge.setPaymentIntent(paymentIntent);

        Refund refund = Refund.builder()
                .amount(500L)
                .reason("duplicate")
                .stripeId("refund_1")
                .charge(charge)
                .created(System.currentTimeMillis())
                .currency(currency)
                .paymentIntent(paymentIntent)
                .status("succeeded")
                .receiptNumber("12345ABC")
                .build();


        charge.getRefunds().add(refund);
        paymentIntent.setCharge(charge);

        // When
        PaymentIntent savedPaymentIntent = paymentIntentRepository.save(paymentIntent);

        // Then
        assertNotNull(savedPaymentIntent.getCharge(), "PaymentIntent should have a charge.");
        assertEquals(1, savedPaymentIntent.getCharge().getRefunds().size(), "Charge should have one refund.");
        assertEquals(500L, savedPaymentIntent.getCharge().getRefunds().get(0).getAmount(),
                     "Refund amount should be 500."
        );
    }

    @Test
    void givenPaymentIntentWithSearchKeyword_whenFindingAll_thenReturnMatchingResults() {
        // Given
        Pageable pageable = PagingTestHelper.createPageRequest(0, 5, "created", Sort.Direction.ASC);

        // When
        Page<PaymentIntent> result = paymentIntentRepository.findAll("pi_stripe_12345", pageable);

        // Then
        PagingTestHelper.assertPagingResults(result, 1, 1, 1, "created", true);
        assertEquals("pi_stripe_12345", result.getContent().get(0).getStripeId(),
                     "PaymentIntent should match the search keyword."
        );
    }

    @Test
    void givenPaymentIntent_whenDeleting_thenRemovePaymentIntent() {
        // Given
        PaymentIntent paymentIntent = createPaymentIntent();
        PaymentIntent savedPaymentIntent = paymentIntentRepository.save(paymentIntent);
        Integer paymentIntentId = savedPaymentIntent.getId();

        // When
        paymentIntentRepository.deleteById(paymentIntentId);

        // Then
        Optional<PaymentIntent> deletedPaymentIntent = paymentIntentRepository.findById(paymentIntentId);
        assertTrue(deletedPaymentIntent.isEmpty(), "PaymentIntent should be deleted.");
    }

    private PaymentIntent createPaymentIntent() {
        PaymentIntent paymentIntent = new PaymentIntent();
        paymentIntent.setStripeId("stripeId_1");
        paymentIntent.setAmount(5000L);
        paymentIntent.setShippingAmount(100L);
        paymentIntent.setTaxAmount(50L);
        paymentIntent.setCustomer(customer);
        paymentIntent.setOrder(
                Order.builder()
                        .transaction(paymentIntent)
                        .orderTime(LocalDateTime.now())
                        .build()
        );
        paymentIntent.setShippingAddress(address);
        paymentIntent.setCourier(courier);
        paymentIntent.setStatus("created");
        paymentIntent.setCreated(System.currentTimeMillis());
        paymentIntent.setTargetCurrency(currency);
        paymentIntent.setShippingTax(100L);
        return paymentIntent;
    }
}

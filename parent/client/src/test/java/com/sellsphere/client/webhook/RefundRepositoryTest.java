package com.sellsphere.client.webhook;

import com.sellsphere.common.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RefundRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RefundRepository refundRepository;

    @Test
    void givenValidRefund_whenSaved_thenShouldBeRetrievableFromDatabase() {
        // Given
        Currency currency = Currency.builder()
                .code("USD")
                .symbol("$")
                .name("US Dollar")
                .unitAmount(BigDecimal.valueOf(100L))
                .build();
        currency = entityManager.persistAndFlush(currency);

        BalanceTransaction balanceTransaction = BalanceTransaction.builder()
                .stripeId("bt_test_id")
                .amount(1000L)
                .created(System.currentTimeMillis())
                .fee(100L)
                .currency(currency)
                .net(900L)
                .exchangeRate(BigDecimal.valueOf(1.0))
                .build();
        balanceTransaction = entityManager.persistAndFlush(balanceTransaction);

        Customer customer = Customer.builder()
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .enabled(true)
                .emailVerified(true)
                .createdTime(LocalDateTime.now())
                .build();
        customer = entityManager.persistAndFlush(customer);

        Country country = Country.builder()
                .name("United States")
                .code("USA")
                .currency(currency)
                .build();
        country = entityManager.persistAndFlush(country);

        Courier courier = Courier.builder()
                .maxDeliveryTime(2)
                .minDeliveryTime(2)
                .name("UPS")
                .logoUrl("logo")
                .build();
        courier = entityManager.persistAndFlush(courier);

        Address address = Address.builder()
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("1234567890")
                .addressLine1("123 Main St")
                .city("Springfield")
                .postalCode("62704")
                .country(country)
                .primary(true)
                .customer(customer)
                .build();
        address = entityManager.persistAndFlush(address);

        PaymentIntent paymentIntent = PaymentIntent.builder()
                .stripeId("pi_test_id")
                .amount(1000L)
                .shippingAmount(100L)
                .shippingTax(10L)
                .taxAmount(50L)
                .exchangeRate(BigDecimal.valueOf(1.0))
                .baseCurrency(currency)
                .targetCurrency(currency)
                .customer(customer)
                .shippingAddress(address)
                .courier(courier)
                .status("requires_payment_method")
                .created(System.currentTimeMillis())
                .build();
        paymentIntent = entityManager.persistAndFlush(paymentIntent);

        Charge charge = Charge.builder()
                .stripeId("ch_test_id")
                .amount(1000L)
                .amountRefunded(0L)
                .balanceTransaction(balanceTransaction)
                .receiptUrl("http://example.com/receipt")
                .refunded(false)
                .status("succeeded")
                .paymentIntent(paymentIntent)
                .build();
        charge = entityManager.persistAndFlush(charge);

        Refund refund = Refund.builder()
                .stripeId("re_test_id")
                .charge(charge)
                .object("refund")
                .amount(1000L)
                .balanceTransaction(balanceTransaction)
                .created(System.currentTimeMillis())
                .currency(currency)
                .paymentIntent(paymentIntent)
                .reason("requested_by_customer")
                .receiptNumber("123456")
                .status("succeeded")
                .failureReason(null)
                .build();

        // When
        Refund savedRefund = refundRepository.save(refund);

        // Then
        Refund foundRefund = entityManager.find(Refund.class, savedRefund.getId());
        assertThat(foundRefund).isNotNull();
        assertThat(foundRefund.getStripeId()).isEqualTo("re_test_id");
        assertThat(foundRefund.getCharge().getStripeId()).isEqualTo("ch_test_id");
        assertThat(foundRefund.getObject()).isEqualTo("refund");
        assertThat(foundRefund.getAmount()).isEqualTo(1000L);
        assertThat(foundRefund.getBalanceTransaction().getStripeId()).isEqualTo("bt_test_id");
        assertThat(foundRefund.getCurrency().getCode()).isEqualTo("USD");
        assertThat(foundRefund.getPaymentIntent().getStripeId()).isEqualTo("pi_test_id");
        assertThat(foundRefund.getReason()).isEqualTo("requested_by_customer");
        assertThat(foundRefund.getReceiptNumber()).isEqualTo("123456");
        assertThat(foundRefund.getStatus()).isEqualTo("succeeded");
        assertThat(foundRefund.getFailureReason()).isNull();
    }

    @Test
    void givenValidStripeId_whenFindByStripeId_thenShouldBeRetrievableFromDatabase() {
        // Given
        Currency currency = Currency.builder()
                .code("USD")
                .symbol("$")
                .name("US Dollar")
                .unitAmount(BigDecimal.valueOf(100L))
                .build();
        currency = entityManager.persistAndFlush(currency);

        BalanceTransaction balanceTransaction = BalanceTransaction.builder()
                .stripeId("bt_test_id")
                .amount(1000L)
                .created(System.currentTimeMillis())
                .fee(100L)
                .currency(currency)
                .net(900L)
                .exchangeRate(BigDecimal.valueOf(1.0))
                .build();
        balanceTransaction = entityManager.persistAndFlush(balanceTransaction);

        Customer customer = Customer.builder()
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .enabled(true)
                .emailVerified(true)
                .createdTime(LocalDateTime.now())
                .build();
        customer = entityManager.persistAndFlush(customer);

        Country country = Country.builder()
                .name("United States")
                .code("USA")
                .currency(currency)
                .build();
        country = entityManager.persistAndFlush(country);

        Address address = Address.builder()
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("1234567890")
                .addressLine1("123 Main St")
                .city("Springfield")
                .postalCode("62704")
                .country(country)
                .primary(true)
                .customer(customer)
                .build();
        address = entityManager.persistAndFlush(address);

        Courier courier = Courier.builder()
                .maxDeliveryTime(2)
                .minDeliveryTime(2)
                .name("UPS")
                .logoUrl("logo")
                .build();
        courier = entityManager.persistAndFlush(courier);

        PaymentIntent paymentIntent = PaymentIntent.builder()
                .stripeId("pi_test_id")
                .amount(1000L)
                .shippingAmount(100L)
                .shippingTax(10L)
                .taxAmount(50L)
                .exchangeRate(BigDecimal.valueOf(1.0))
                .baseCurrency(currency)
                .targetCurrency(currency)
                .customer(customer)
                .courier(courier)
                .status("refunded")
                .shippingAddress(address)
                .created(System.currentTimeMillis())
                .build();
        paymentIntent = entityManager.persistAndFlush(paymentIntent);

        Charge charge = Charge.builder()
                .stripeId("ch_test_id")
                .amount(1000L)
                .amountRefunded(0L)
                .balanceTransaction(balanceTransaction)
                .receiptUrl("http://example.com/receipt")
                .refunded(false)
                .status("succeeded")
                .paymentIntent(paymentIntent)
                .build();
        charge = entityManager.persistAndFlush(charge);

        Refund refund = Refund.builder()
                .stripeId("re_test_id")
                .charge(charge)
                .object("refund")
                .amount(1000L)
                .balanceTransaction(balanceTransaction)
                .created(System.currentTimeMillis())
                .currency(currency)
                .paymentIntent(paymentIntent)
                .reason("requested_by_customer")
                .receiptNumber("123456")
                .status("succeeded")
                .failureReason(null)
                .build();
        refund = entityManager.persistAndFlush(refund);

        // When
        Optional<Refund> foundRefund = refundRepository.findByStripeId("re_test_id");

        // Then
        assertThat(foundRefund).isPresent();
        assertThat(foundRefund.get().getStripeId()).isEqualTo("re_test_id");
        assertThat(foundRefund.get().getCharge().getStripeId()).isEqualTo("ch_test_id");
        assertThat(foundRefund.get().getObject()).isEqualTo("refund");
        assertThat(foundRefund.get().getAmount()).isEqualTo(1000L);
        assertThat(foundRefund.get().getBalanceTransaction().getStripeId()).isEqualTo("bt_test_id");
        assertThat(foundRefund.get().getCurrency().getCode()).isEqualTo("USD");
        assertThat(foundRefund.get().getPaymentIntent().getStripeId()).isEqualTo("pi_test_id");
        assertThat(foundRefund.get().getReason()).isEqualTo("requested_by_customer");
        assertThat(foundRefund.get().getReceiptNumber()).isEqualTo("123456");
        assertThat(foundRefund.get().getStatus()).isEqualTo("succeeded");
        assertThat(foundRefund.get().getFailureReason()).isNull();
    }
}

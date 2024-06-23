package com.sellsphere.payment.checkout;

import com.stripe.exception.StripeException;
import com.stripe.model.CustomerSession;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.model.tax.Calculation;
import com.stripe.model.tax.Transaction;
import com.stripe.param.CustomerSessionCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentIntentUpdateParams;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.tax.CalculationCreateParams;
import com.stripe.param.tax.TransactionCreateFromCalculationParams;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StripeCheckoutServiceTest {

    @Mock
    private PaymentIntent paymentIntentMock;

    @Mock
    private Refund refundMock;

    @Mock
    private Transaction transactionMock;

    @Mock
    private Calculation calculationMock;

    @Mock
    private CustomerSession customerSessionMock;

    @InjectMocks
    private StripeCheckoutService stripeCheckoutService;

    @Test
    void shouldCreatePaymentIntent_WhenValidParamsAreProvided() throws StripeException {
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(1000L)
                .setCurrency("usd")
                .build();

        try (MockedStatic<PaymentIntent> paymentIntentMockedStatic = mockStatic(PaymentIntent.class)) {
            paymentIntentMockedStatic.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class))).thenReturn(paymentIntentMock);

            PaymentIntent result = stripeCheckoutService.createPaymentIntent(params);

            assertNotNull(result);
            assertEquals(paymentIntentMock, result);
            paymentIntentMockedStatic.verify(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)), times(1));
        }
    }

    @Test
    void shouldCreateRefund_WhenValidParamsAreProvided() throws StripeException {
        String paymentIntentId = "pi_123";
        Long amount = 500L;
        RefundCreateParams.Reason reason = RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER;

        try (MockedStatic<Refund> refundMockedStatic = mockStatic(Refund.class)) {
            refundMockedStatic.when(() -> Refund.create(any(RefundCreateParams.class))).thenReturn(refundMock);

            Refund result = stripeCheckoutService.createRefund(paymentIntentId, amount, reason);

            assertNotNull(result);
            assertEquals(refundMock, result);
            refundMockedStatic.verify(() -> Refund.create(any(RefundCreateParams.class)), times(1));
        }
    }

    @Test
    void should_CreateTransaction_WhenValidPaymentIntentIsProvided() throws StripeException {
        when(paymentIntentMock.getMetadata()).thenReturn(Map.of("calculation_id", "calc_123"));
        when(paymentIntentMock.getId()).thenReturn("pi_123");

        try (MockedStatic<Transaction> transactionMockedStatic = mockStatic(Transaction.class)) {
            transactionMockedStatic.when(() -> Transaction.createFromCalculation(any(TransactionCreateFromCalculationParams.class))).thenReturn(transactionMock);
            when(transactionMock.getId()).thenReturn("tx_123");
            when(paymentIntentMock.update(any(PaymentIntentUpdateParams.class))).thenReturn(paymentIntentMock);

            Transaction result = stripeCheckoutService.createTransaction(paymentIntentMock);

            assertNotNull(result);
            assertEquals(transactionMock, result);
            transactionMockedStatic.verify(() -> Transaction.createFromCalculation(any(TransactionCreateFromCalculationParams.class)), times(1));
            verify(paymentIntentMock, times(1)).update(any(PaymentIntentUpdateParams.class));
        }
    }

    @Test
    void should_CalculateTaxes_WhenValidParamsAreProvided() throws StripeException {
        CalculationCreateParams params = CalculationCreateParams.builder()
                .setCurrency("usd")
                .build();

        try (MockedStatic<Calculation> calculationMockedStatic = mockStatic(Calculation.class)) {
            calculationMockedStatic.when(() -> Calculation.create(any(CalculationCreateParams.class))).thenReturn(calculationMock);

            Calculation result = stripeCheckoutService.calculate(params);

            assertNotNull(result);
            assertEquals(calculationMock, result);
            calculationMockedStatic.verify(() -> Calculation.create(any(CalculationCreateParams.class)), times(1));
        }
    }

    @Test
    void should_CreateCustomerSession_WhenValidStripeIdIsProvided() throws StripeException {
        String stripeId = "cus_123";

        try (MockedStatic<CustomerSession> customerSessionMockedStatic = mockStatic(CustomerSession.class)) {
            customerSessionMockedStatic.when(() -> CustomerSession.create(any(CustomerSessionCreateParams.class))).thenReturn(customerSessionMock);

            CustomerSession result = stripeCheckoutService.createCustomerSession(stripeId);

            assertNotNull(result, "The result should not be null");
            assertEquals(customerSessionMock, result, "The result should be the mock customer session");
            customerSessionMockedStatic.verify(() -> CustomerSession.create(any(CustomerSessionCreateParams.class)), times(1));
        }
    }
}

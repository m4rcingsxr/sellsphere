package com.sellsphere.admin.transaction;

import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.common.entity.PaymentIntent;
import com.sellsphere.common.entity.TransactionNotFoundException;
import com.sellsphere.payment.checkout.StripeCheckoutService;
import com.stripe.param.RefundCreateParams;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Sql(scripts = {"classpath:sql/payment_intents.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class TransactionServiceIntegrationTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private PaymentIntentRepository paymentIntentRepository;

    @MockBean
    private StripeCheckoutService stripeCheckoutService;

    @Test
    void givenExistingTransactionId_whenFindTransactionById_thenReturnTransaction() throws TransactionNotFoundException {
        // Given
        Integer transactionId = 1;

        // When
        PaymentIntent paymentIntent = transactionService.findTransactionById(transactionId);

        // Then
        assertNotNull(paymentIntent);
        assertEquals("pi_stripe_12345", paymentIntent.getStripeId());
        assertEquals(5000L, paymentIntent.getAmount());
    }

    @Test
    void givenNonExistingTransactionId_whenFindTransactionById_thenThrowTransactionNotFoundException() {
        // Given
        Integer nonExistingTransactionId = 999;

        // When / Then
        assertThrows(TransactionNotFoundException.class, () -> transactionService.findTransactionById(nonExistingTransactionId));
    }

    @Test
    void whenListTransactionsWithPaging_thenReturnPagedResults() {
        // Given
        int pageNum = 0;
        PagingAndSortingHelper helper = new PagingAndSortingHelper(new ModelAndViewContainer(), "transactionList", "created", Sort.Direction.ASC, null);

        // When
        transactionService.listEntities(helper, pageNum);

        // Then
        List<PaymentIntent> transactions = helper.getContent();
        assertNotNull(transactions);
        assertEquals(2, transactions.size());
    }

    @Test
    void givenValidPaymentIntentIdAndAmount_whenCreateRefund_thenRefundIsCreatedSuccessfully() throws Exception {
        // Given
        Integer paymentIntentId = 1;
        BigDecimal refundAmount = BigDecimal.valueOf(10L);  // $10 refund
        String reason = "DUPLICATE";
        com.stripe.model.Refund stripeRefundMock = new com.stripe.model.Refund();
        stripeRefundMock.setAmount(1000L);  // Mocked Stripe refund amount
        stripeRefundMock.setReason("DUPLICATE");
        stripeRefundMock.setStatus("succeeded");
        stripeRefundMock.setId("r_123");  // Stripe refund ID
        stripeRefundMock.setCreated(1698060900L);


        // Mock Stripe service to return the mocked refund
        Mockito.when(stripeCheckoutService.createRefund(Mockito.anyString(), Mockito.anyLong(), Mockito.any(RefundCreateParams.Reason.class)))
                .thenReturn(stripeRefundMock);

        // When
        com.sellsphere.common.entity.Refund refund = transactionService.createRefund(paymentIntentId, refundAmount, reason);

        // Then
        assertNotNull(refund);
        assertEquals(1000L, refund.getAmount());  // Stripe refund amount
        assertEquals("DUPLICATE", refund.getReason());
    }

    @Test
    void givenTransactionId_whenGetAvailableRefund_thenReturnCorrectAvailableAmount() throws TransactionNotFoundException {
        // Given
        Integer transactionId = 1;

        // When
        BigDecimal availableRefund = transactionService.getAvailableRefund(transactionId);

        // Then
        assertNotNull(availableRefund);
        assertEquals(BigDecimal.valueOf(50), availableRefund);
    }

    @Test
    void givenNonExistingTransactionId_whenGetAvailableRefund_thenThrowTransactionNotFoundException() {
        // Given
        Integer nonExistingTransactionId = 999;

        // When / Then
        assertThrows(TransactionNotFoundException.class, () -> transactionService.getAvailableRefund(nonExistingTransactionId));
    }

    @Test
    void whenFindAll_thenReturnAllTransactionsSortedByCreatedDate() {
        // When
        List<PaymentIntent> transactions = transactionService.findAll();

        // Then
        assertNotNull(transactions);
        assertEquals(2, transactions.size());  // Ensure two transactions are returned
        assertEquals("pi_stripe_12345", transactions.get(0).getStripeId());  // Ensure correct sorting
        assertEquals("pi_stripe_54321", transactions.get(1).getStripeId());
    }


}

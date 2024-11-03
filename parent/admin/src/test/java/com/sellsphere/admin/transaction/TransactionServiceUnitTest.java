package com.sellsphere.admin.transaction;

import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.common.entity.*;
import com.sellsphere.payment.checkout.StripeCheckoutService;
import com.stripe.model.Refund;
import com.stripe.param.RefundCreateParams;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class TransactionServiceUnitTest {

    @Mock
    private PaymentIntentRepository transactionRepository;

    @Mock
    private StripeCheckoutService stripeCheckoutService;

    @Mock
    private RefundRepository refundRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void givenPagingHelper_whenListEntities_thenEntitiesAreListedWithPaginationAndSorting() {
        // Given
        PagingAndSortingHelper helperMock = mock(PagingAndSortingHelper.class);
        int pageNum = 0;

        // When
        transactionService.listEntities(helperMock, pageNum);

        // Then
        then(helperMock).should().listEntities(pageNum, 10, transactionRepository);
    }

    @Test
    void givenValidPaymentIntentIdAndAmount_whenCreateRefund_thenRefundIsCreatedSuccessfully() throws Exception {
        // Given
        Integer paymentId = 1;
        BigDecimal refundAmount = BigDecimal.valueOf(100L);
        String reason = "DUPLICATE";
        String stripeId = "S_123";
        BigDecimal unitAmount = BigDecimal.valueOf(100L);

        PaymentIntent paymentIntentMock = mock(PaymentIntent.class);
        Currency currencyMock = mock(Currency.class);
        Refund refundMock = mock(Refund.class); // Mock the Stripe Refund object
        com.sellsphere.common.entity.Refund platformRefundMock = mock(com.sellsphere.common.entity.Refund.class);

        given(paymentIntentMock.getTargetCurrency()).willReturn(currencyMock);
        given(currencyMock.getUnitAmount()).willReturn(unitAmount);
        given(paymentIntentMock.getStripeId()).willReturn(stripeId);
        given(transactionRepository.findById(paymentId)).willReturn(Optional.of(paymentIntentMock));
        given(refundMock.getId()).willReturn("stripe_refund_id");
        given(stripeCheckoutService.createRefund(eq(stripeId), anyLong(), any(RefundCreateParams.Reason.class)))
                .willReturn(refundMock);
        given(refundRepository.save(any(com.sellsphere.common.entity.Refund.class))).willReturn(platformRefundMock);

        // When
        com.sellsphere.common.entity.Refund result = transactionService.createRefund(paymentId, refundAmount, reason);

        // Then
        assertNotNull(result); // Ensure the refund is not null
        then(refundRepository).should().save(any(com.sellsphere.common.entity.Refund.class));
    }


    @Test
    void givenValidTransactionId_whenGetAvailableRefund_thenCorrectRefundAmountIsReturned() throws Exception {
        // Given
        Integer transactionId = 1;
        PaymentIntent transactionMock = mock(PaymentIntent.class);
        Currency currencyMock = mock(Currency.class);
        Charge chargeMock = mock(Charge.class); // Mock the Charge object

        // Stubbing
        given(transactionRepository.findById(transactionId)).willReturn(Optional.of(transactionMock));
        given(transactionMock.getTargetCurrency()).willReturn(currencyMock);
        given(currencyMock.getUnitAmount()).willReturn(BigDecimal.valueOf(100L));
        given(transactionMock.getCharge()).willReturn(chargeMock); // Mocking getCharge() to return chargeMock
        given(chargeMock.getAmountRefunded()).willReturn(200L); // Mocking amount refunded
        given(chargeMock.getAmount()).willReturn(1000L); // Mocking total amount

        // Mock static MoneyUtil method
        try (MockedStatic<MoneyUtil> mockedMoneyUtil = mockStatic(MoneyUtil.class)) {
            mockedMoneyUtil.when(() -> MoneyUtil.convertToDisplayPrice(800L, BigDecimal.valueOf(100L)))
                    .thenReturn(BigDecimal.valueOf(8L));

            // When
            BigDecimal availableRefund = transactionService.getAvailableRefund(transactionId);

            // Then
            assertNotNull(availableRefund); // Ensure the available refund is not null
            assertEquals(BigDecimal.valueOf(8L), availableRefund); // Verify the correct refund amount
            mockedMoneyUtil.verify(() -> MoneyUtil.convertToDisplayPrice(800L, BigDecimal.valueOf(100L)));
        }
    }


    @Test
    void givenInvalidTransactionId_whenGetAvailableRefund_thenThrowsTransactionNotFoundException() {
        // Given
        Integer invalidTransactionId = 999;
        given(transactionRepository.findById(invalidTransactionId)).willReturn(Optional.empty());

        // When / Then
        assertThrows(TransactionNotFoundException.class, () -> {
            transactionService.getAvailableRefund(invalidTransactionId);
        });
    }

    @Test
    void givenValidTransactionId_whenFindTransactionById_thenTransactionIsReturned() throws Exception {
        // Given
        Integer transactionId = 1;
        PaymentIntent transactionMock = mock(PaymentIntent.class);
        given(transactionRepository.findById(transactionId)).willReturn(Optional.of(transactionMock));

        // When
        PaymentIntent foundTransaction = transactionService.findTransactionById(transactionId);

        // Then
        assertNotNull(foundTransaction); // Ensure the transaction is not null
        then(transactionRepository).should().findById(transactionId);
    }

    @Test
    void givenInvalidTransactionId_whenFindTransactionById_thenThrowsTransactionNotFoundException() {
        // Given
        Integer invalidTransactionId = 999;
        given(transactionRepository.findById(invalidTransactionId)).willReturn(Optional.empty());

        // When / Then
        assertThrows(TransactionNotFoundException.class, () -> {
            transactionService.findTransactionById(invalidTransactionId);
        });
    }

    @Test
    void whenFindAll_thenReturnsSortedTransactionList() {
        // Given
        PaymentIntent transaction1 = mock(PaymentIntent.class);
        PaymentIntent transaction2 = mock(PaymentIntent.class);
        List<PaymentIntent> mockTransactionList = Arrays.asList(transaction1, transaction2);
        given(transactionRepository.findAll(Sort.by(Sort.Direction.ASC, "created"))).willReturn(mockTransactionList);

        // When
        List<PaymentIntent> transactions = transactionService.findAll();

        // Then
        assertNotNull(transactions);
        assertEquals(2, transactions.size());
        then(transactionRepository).should().findAll(Sort.by(Sort.Direction.ASC, "created"));
    }

}
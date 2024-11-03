package com.sellsphere.admin.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sellsphere.common.entity.*;
import com.sellsphere.common.entity.payload.RefundDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class TransactionRestControllerTest {

    @MockBean
    private TransactionService transactionService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void givenValidRefundRequest_whenRequestRefund_thenRefundIsProcessedSuccessfully() throws Exception {
        // Given
        Currency eur = Currency.builder()
                .code("EUR")
                .unitAmount(BigDecimal.valueOf(100.00))
                .build();

        PaymentIntent paymentIntentMock = mock(PaymentIntent.class);
        given(paymentIntentMock.getTotalRefunded()).willReturn(BigDecimal.ZERO);
        given(paymentIntentMock.getDisplayAmount()).willReturn(BigDecimal.valueOf(10.00));
        given(paymentIntentMock.getTargetCurrency()).willReturn(eur);


        given(transactionService.getAvailableRefund(anyInt())).willReturn(BigDecimal.valueOf(10.00));
        given(transactionService.createRefund(anyInt(), any(BigDecimal.class), anyString())).willReturn(
                Refund.builder()
                        .currency(eur)
                        .amount(1000L)
                        .paymentIntent(paymentIntentMock)
                        .status("succeeded")
                        .build()
        );

        // When / Then
        mockMvc.perform(post("/transactions/refund")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        RefundDTO.builder()
                                                .paymentIntent(1)
                                                .amount(BigDecimal.valueOf(10.00))
                                                .reason("DUPLICATE")
                                                .build()
                                )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("refunded"))
                .andExpect(jsonPath("$.refunded").value(true))
                .andExpect(jsonPath("$.refundedAmount").value(MoneyUtil.formatAmount(BigDecimal.valueOf(10.00), eur.getCode())));

        then(transactionService).should().getAvailableRefund(1);
        then(transactionService).should().createRefund(eq(1), eq(BigDecimal.valueOf(10.00)), eq("DUPLICATE"));
    }

    @Test
    void givenValidTransactionId_whenGetAvailableRefund_thenReturnAvailableRefund() throws Exception {
        // Given
        given(transactionService.getAvailableRefund(anyInt())).willReturn(BigDecimal.valueOf(100.00));

        // When / Then
        mockMvc.perform(get("/transactions/refunds/available")
                                .param("id", "1")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableRefund").value(100.00));

        then(transactionService).should().getAvailableRefund(1);
    }

    @Test
    void givenValidTransactionIdAndRefundAmount_whenCheckRefundEligibility_thenReturnApproved() throws Exception {
        // Given
        given(transactionService.getAvailableRefund(anyInt())).willReturn(BigDecimal.valueOf(100.00));

        // When / Then
        mockMvc.perform(get("/transactions/refunds/eligibility")
                                .param("id", "1")
                                .param("amount", "50")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approve").value(true))
                .andExpect(jsonPath("$.availableRefund").value(100.00));

        then(transactionService).should().getAvailableRefund(1);
    }

    @Test
    void givenInvalidRefundAmount_whenCheckRefundEligibility_thenReturnNotApproved() throws Exception {
        // Given
        given(transactionService.getAvailableRefund(anyInt())).willReturn(BigDecimal.valueOf(50.00));

        // When / Then
        mockMvc.perform(get("/transactions/refunds/eligibility")
                                .param("id", "1")
                                .param("amount", "100")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approve").value(false))
                .andExpect(jsonPath("$.availableRefund").value(50.00));

        then(transactionService).should().getAvailableRefund(1);
    }

    @Test
    void givenInvalidTransactionId_whenRequestRefund_thenThrowTransactionNotFoundException() throws Exception {
        // Given
        given(transactionService.getAvailableRefund(anyInt())).willThrow(new TransactionNotFoundException("Transaction not found"));

        // When / Then
        mockMvc.perform(post("/transactions/refund")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        RefundDTO.builder()
                                                .paymentIntent(999)
                                                .amount(BigDecimal.valueOf(100.00))
                                                .reason("DUPLICATE")
                                                .build()
                                )))
                .andExpect(status().isNotFound());

        then(transactionService).should().getAvailableRefund(999);
    }

    @Test
    void givenExceedingRefundAmount_whenRequestRefund_thenThrowIllegalArgumentException() throws Exception {
        // Given
        given(transactionService.getAvailableRefund(anyInt())).willReturn(BigDecimal.valueOf(50.00));

        // When / Then
        mockMvc.perform(post("/transactions/refund")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        RefundDTO.builder()
                                                .paymentIntent(1)
                                                .amount(BigDecimal.valueOf(100.00))
                                                .reason("DUPLICATE")
                                                .build()
                                )))
                .andExpect(status().isBadRequest());

        then(transactionService).should().getAvailableRefund(1);
    }

    @Test
    void givenValidRefundRequest_whenRequestRefundWithPartialAmount_thenPartialRefundIsProcessedSuccessfully() throws Exception {
        // Given
        Currency eur = Currency.builder()
                .code("EUR")
                .unitAmount(BigDecimal.valueOf(100.00))
                .build();

        PaymentIntent paymentIntentMock = mock(PaymentIntent.class);
        given(paymentIntentMock.getTotalRefunded()).willReturn(BigDecimal.ZERO);
        given(paymentIntentMock.getDisplayAmount()).willReturn(BigDecimal.valueOf(10.00));
        given(paymentIntentMock.getTargetCurrency()).willReturn(eur);

        given(transactionService.getAvailableRefund(anyInt())).willReturn(BigDecimal.valueOf(10.00));
        given(transactionService.createRefund(anyInt(), any(BigDecimal.class), anyString())).willReturn(
                Refund.builder()
                        .currency(eur)
                        .amount(500L)
                        .paymentIntent(paymentIntentMock)
                        .status("succeeded")
                        .build()
        );

        // When / Then
        mockMvc.perform(post("/transactions/refund")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        RefundDTO.builder()
                                                .paymentIntent(1)
                                                .amount(BigDecimal.valueOf(5.00))
                                                .reason("DUPLICATE")
                                                .build()
                                )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Partial Refund"))
                .andExpect(jsonPath("$.refunded").value(false));

        then(transactionService).should().getAvailableRefund(1);
        then(transactionService).should().createRefund(eq(1), eq(BigDecimal.valueOf(5.00)), eq("DUPLICATE"));
    }
}


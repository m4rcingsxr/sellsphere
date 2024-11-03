package com.sellsphere.admin.report;

import com.sellsphere.admin.order.OrderDetailRepository;
import com.sellsphere.common.entity.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class OrderDetailSalesReportServiceTest {

    @Mock
    private OrderDetailRepository orderDetailRepository;

    @InjectMocks
    private OrderDetailSalesReportService orderDetailSalesReportService;

    @Test
    void givenValidDatesAndCategoryReportType_whenGetSalesReportByDateRange_thenReturnCategoryReport() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        SaleReportType reportType = SaleReportType.CATEGORY;

        // Mock Category, Product, and Order
        Category category = Category.builder().name("Electronics").build();
        Product product = Product.builder().name("Laptop").category(category).build();

        // Create Currency needed for PaymentIntent
        Currency currency = Currency.builder()
                .code("USD")
                .unitAmount(BigDecimal.valueOf(100))
                .build();

        // Create PaymentIntent
        PaymentIntent paymentIntent = PaymentIntent.builder()
                .stripeId("pi_123456789")
                .amount(3000L) // Total amount in smallest currency unit (e.g., cents)
                .shippingAmount(500L)
                .shippingTax(50L)
                .taxAmount(250L)
                .targetCurrency(currency)
                .status("succeeded")
                .created(System.currentTimeMillis())
                .courier(Courier.builder().maxDeliveryTime(5).build())
                .build();

        // Create Order and set transaction
        Order order = Order.builder()
                .orderTime(LocalDateTime.now())
                .transaction(paymentIntent)
                .build();

        // Mock OrderDetails
        OrderDetail orderDetail1 = OrderDetail.builder()
                .product(product)
                .order(order)
                .quantity(1)
                .productCost(BigDecimal.valueOf(1000))
                .productPrice(BigDecimal.valueOf(1200))
                .subtotal(BigDecimal.valueOf(1200))
                .build();

        OrderDetail orderDetail2 = OrderDetail.builder()
                .product(product)
                .order(order)
                .quantity(1)
                .productCost(BigDecimal.valueOf(1500))
                .productPrice(BigDecimal.valueOf(1800))
                .subtotal(BigDecimal.valueOf(1800))
                .build();

        List<OrderDetail> mockOrderDetails = List.of(orderDetail1, orderDetail2);

        given(orderDetailRepository.findAllWithCategoryNameAndTimeBetween(
                ArgumentMatchers.any(), ArgumentMatchers.any())).willReturn(mockOrderDetails);

        // When
        ReportResponse reportResponse = orderDetailSalesReportService.getSalesReportByDateRange(
                startDate, endDate, reportType);

        // Then
        assertEquals(1, reportResponse.getReports().size());
        ReportUnit reportUnit = reportResponse.getReports().get(0);
        assertEquals("Electronics", reportUnit.getIdentifier());
        assertEquals(BigDecimal.valueOf(3000), reportUnit.getGrossSales());
        assertEquals("0.00", reportResponse.getFee().toPlainString());
        assertEquals(paymentIntent.getDisplayShippingAmount(), reportResponse.getShippingCost());

        // Verify
        then(orderDetailRepository).should().findAllWithCategoryNameAndTimeBetween(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    void givenValidDatesAndProductReportType_whenGetSalesReportByDateRange_thenReturnProductReport() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        SaleReportType reportType = SaleReportType.PRODUCT;

        // Mock Category, Products, and Order
        Category category = Category.builder().name("Electronics").build();
        Product product1 = Product.builder().name("Laptop").category(category).build();
        Product product2 = Product.builder().name("TV").category(category).build();

        // Create Currency needed for PaymentIntent
        Currency currency = Currency.builder()
                .code("USD")
                .unitAmount(BigDecimal.valueOf(100))
                .build();

        // Create PaymentIntent
        PaymentIntent paymentIntent = PaymentIntent.builder()
                .stripeId("pi_987654321")
                .amount(3000L)
                .shippingAmount(500L)
                .shippingTax(50L)
                .taxAmount(250L)
                .targetCurrency(currency)
                .status("succeeded")
                .created(System.currentTimeMillis())
                .courier(Courier.builder().maxDeliveryTime(5).build())
                .build();

        // Create Order and set transaction
        Order order = Order.builder()
                .orderTime(LocalDateTime.now())
                .transaction(paymentIntent)
                .build();

        // Mock OrderDetails
        OrderDetail orderDetail1 = OrderDetail.builder()
                .product(product1)
                .order(order)
                .quantity(1)
                .productCost(BigDecimal.valueOf(1000))
                .productPrice(BigDecimal.valueOf(1200))
                .subtotal(BigDecimal.valueOf(1200))
                .build();

        OrderDetail orderDetail2 = OrderDetail.builder()
                .product(product2)
                .order(order)
                .quantity(1)
                .productCost(BigDecimal.valueOf(1500))
                .productPrice(BigDecimal.valueOf(1800))
                .subtotal(BigDecimal.valueOf(1800))
                .build();

        List<OrderDetail> mockOrderDetails = List.of(orderDetail1, orderDetail2);

        given(orderDetailRepository.findAllWithProductNameAndTimeBetween(
                ArgumentMatchers.any(), ArgumentMatchers.any())).willReturn(mockOrderDetails);

        // When
        ReportResponse reportResponse = orderDetailSalesReportService.getSalesReportByDateRange(
                startDate, endDate, reportType);

        // Then
        assertEquals(2, reportResponse.getReports().size());
        ReportUnit reportUnit1 = reportResponse.getReports().get(0);
        ReportUnit reportUnit2 = reportResponse.getReports().get(1);

        assertEquals("Laptop", reportUnit1.getIdentifier());
        assertEquals(BigDecimal.valueOf(1200), reportUnit1.getGrossSales());

        assertEquals("TV", reportUnit2.getIdentifier());
        assertEquals(BigDecimal.valueOf(1800), reportUnit2.getGrossSales());

        assertEquals("0.00", reportResponse.getFee().toPlainString());
        assertEquals(paymentIntent.getDisplayShippingAmount(), reportResponse.getShippingCost());

        // Verify
        then(orderDetailRepository).should().findAllWithProductNameAndTimeBetween(ArgumentMatchers.any(), ArgumentMatchers.any());
    }
}

package com.sellsphere.admin.report;

import com.sellsphere.admin.order.OrderRepository;
import com.sellsphere.common.entity.Order;
import com.sellsphere.common.entity.PaymentIntent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderSalesReportServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderSalesReportService orderSalesReportService;

    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        startDate = LocalDate.of(2023, 1, 1);
        endDate = LocalDate.of(2023, 1, 31);
    }

    @Test
    void givenValidDatesAndDayReportType_whenGetSalesReportByDateRange_thenReturnDayGroupedReport() {
        List<Order> orders = mockOrderList();
        when(orderRepository.findByOrderTimeBetween(startDate.atStartOfDay(), endDate.atTime(23, 59, 59)))
                .thenReturn(orders);

        try (MockedStatic<SalesReportHelper> mockedStatic = mockStatic(SalesReportHelper.class)) {
            mockedStatic.when(() -> SalesReportHelper.calculateShippingCost(any())).thenReturn(BigDecimal.valueOf(100));
            mockedStatic.when(() -> SalesReportHelper.calculateFee(any())).thenReturn(BigDecimal.valueOf(10));

            ReportResponse response = orderSalesReportService.getSalesReportByDateRangeInternal(startDate, endDate, SaleReportType.DAY);

            assertNotNull(response);
            assertEquals(31, response.getReports().size());
            assertEquals(BigDecimal.valueOf(100), response.getShippingCost());
            assertEquals(BigDecimal.valueOf(10), response.getFee());

            // Verify content of report units for existing sales
            ReportUnit reportUnit = response.getReports().get(14);
            assertEquals("2023-01-15", reportUnit.getIdentifier());
            assertEquals(BigDecimal.valueOf(100), reportUnit.getGrossSales());
            assertEquals(BigDecimal.valueOf(90), reportUnit.getNetSales());
            assertEquals(2, reportUnit.getOrdersCount());

            verify(orderRepository, times(1)).findByOrderTimeBetween(startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
        }
    }

    @Test
    void givenValidDatesAndMonthReportType_whenGetSalesReportByDateRange_thenReturnMonthGroupedReport() {
        // Arrange
        List<Order> orders = mockOrderList();
        when(orderRepository.findByOrderTimeBetween(startDate.atStartOfDay(), endDate.atTime(23, 59, 59)))
                .thenReturn(orders);

        // Mock the static methods from SalesReportHelper
        try (MockedStatic<SalesReportHelper> mockedStatic = mockStatic(SalesReportHelper.class)) {
            mockedStatic.when(() -> SalesReportHelper.calculateShippingCost(any())).thenReturn(BigDecimal.valueOf(100));
            mockedStatic.when(() -> SalesReportHelper.calculateFee(any())).thenReturn(BigDecimal.valueOf(10));

            // Act
            ReportResponse response = orderSalesReportService.getSalesReportByDateRangeInternal(startDate, endDate, SaleReportType.MONTH);

            // Assert
            assertNotNull(response);
            assertEquals(1, response.getReports().size());  // Grouped by month
            assertEquals("2023-01", response.getReports().get(0).getIdentifier());
            assertEquals(BigDecimal.valueOf(100), response.getShippingCost());
            assertEquals(BigDecimal.valueOf(10), response.getFee());

            // Verify content of the report unit for January
            ReportUnit reportUnit = response.getReports().get(0);
            assertEquals("2023-01", reportUnit.getIdentifier());
            assertEquals(BigDecimal.valueOf(100), reportUnit.getGrossSales()); // Update based on actual sales logic
            assertEquals(2, reportUnit.getOrdersCount()); // Assuming two orders in January

            verify(orderRepository, times(1)).findByOrderTimeBetween(startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
        }
    }

    private List<Order> mockOrderList() {
        Order order1 = createMockOrder(BigDecimal.valueOf(50), BigDecimal.valueOf(45));
        Order order2 = createMockOrder(BigDecimal.valueOf(50), BigDecimal.valueOf(45));

        return List.of(order1, order2);
    }

    private Order createMockOrder(BigDecimal grossSales, BigDecimal netSales) {
        Order order = mock(Order.class);
        PaymentIntent transaction = mock(PaymentIntent.class);

        when(transaction.getDisplayAmount()).thenReturn(grossSales);
        when(transaction.getDisplayNet()).thenReturn(netSales);
        when(order.getTransaction()).thenReturn(transaction);
        when(order.getOrderTime()).thenReturn(LocalDate.of(2023, 1, 15).atStartOfDay());

        return order;
    }
}

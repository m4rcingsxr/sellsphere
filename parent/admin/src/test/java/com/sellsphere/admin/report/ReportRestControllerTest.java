package com.sellsphere.admin.report;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ReportRestControllerTest {

    @MockBean
    private OrderSalesReportService salesReportService;

    @MockBean
    private OrderDetailSalesReportService orderDetailSalesReportService;

    @Autowired
    private MockMvc mockMvc;

    private static final LocalDate START_DATE = LocalDate.of(2023, 1, 1);
    private static final LocalDate END_DATE = LocalDate.of(2023, 1, 31);

    @Test
    void givenValidPeriod_whenGetReportDataByPeriod_thenReturnReportResponse() throws Exception {
        ReportResponse mockResponse = createMockReportResponse();
        given(salesReportService.getSalesReportFromLast7Days(SaleReportType.DAY)).willReturn(mockResponse);

        mockMvc.perform(get("/reports/sales/7_days")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reports").isArray())
                .andExpect(jsonPath("$.reports.length()").value(1))
                .andExpect(jsonPath("$.reports[0].identifier").value("2023-01"))
                .andExpect(jsonPath("$.reports[0].grossSales").value(100))
                .andExpect(jsonPath("$.reports[0].ordersCount").value(1))
                .andExpect(jsonPath("$.reports[0].netSales").value(90));

        then(salesReportService).should().getSalesReportFromLast7Days(SaleReportType.DAY);
    }

    @Test
    void givenValidDateRange_whenGetReportDataByDateRange_thenReturnReportResponse() throws Exception {
        ReportResponse mockResponse = createMockReportResponse();
        given(salesReportService.getSalesReportByDateRange(START_DATE, END_DATE, SaleReportType.DAY)).willReturn(mockResponse);

        mockMvc.perform(get("/reports/sales/{start}/{end}", START_DATE, END_DATE)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reports").isArray())
                .andExpect(jsonPath("$.reports.length()").value(1))
                .andExpect(jsonPath("$.reports[0].identifier").value("2023-01"))
                .andExpect(jsonPath("$.reports[0].grossSales").value(100))
                .andExpect(jsonPath("$.reports[0].netSales").value(90))
                .andExpect(jsonPath("$.reports[0].ordersCount").value(1))
                .andExpect(jsonPath("$.shippingCost").value(10))
                .andExpect(jsonPath("$.fee").value(5));

        then(salesReportService).should().getSalesReportByDateRange(START_DATE, END_DATE, SaleReportType.DAY);
    }


    @Test
    void givenValidReportTypeAndPeriod_whenGetReportDataByGroupAndPeriod_thenReturnReportResponse() throws Exception {
        ReportResponse mockResponse = createMockReportResponse();
        given(orderDetailSalesReportService.getSalesReportFromLast7Days(SaleReportType.CATEGORY)).willReturn(mockResponse);

        mockMvc.perform(get("/reports/sales_by_group/CATEGORY/7_days")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reports").isArray())
                .andExpect(jsonPath("$.reports.length()").value(1))
                .andExpect(jsonPath("$.reports[0].identifier").value("2023-01"))
                .andExpect(jsonPath("$.reports[0].grossSales").value(100))
                .andExpect(jsonPath("$.reports[0].netSales").value(90))
                .andExpect(jsonPath("$.reports[0].ordersCount").value(1))
                .andExpect(jsonPath("$.shippingCost").value(10))
                .andExpect(jsonPath("$.fee").value(5));

        then(orderDetailSalesReportService).should().getSalesReportFromLast7Days(SaleReportType.CATEGORY);
    }


    @Test
    void givenValidReportTypeAndDateRange_whenGetReportDataByGroupAndDateRange_thenReturnReportResponse() throws Exception {
        ReportResponse mockResponse = createMockReportResponse();
        given(orderDetailSalesReportService.getSalesReportByDateRange(START_DATE, END_DATE, SaleReportType.PRODUCT)).willReturn(mockResponse);

        mockMvc.perform(get("/reports/sales_by_group/PRODUCT/{start}/{end}", START_DATE, END_DATE)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reports").isArray())
                .andExpect(jsonPath("$.reports.length()").value(1))
                .andExpect(jsonPath("$.reports[0].identifier").value("2023-01"))
                .andExpect(jsonPath("$.reports[0].grossSales").value(100))
                .andExpect(jsonPath("$.reports[0].netSales").value(90))
                .andExpect(jsonPath("$.reports[0].ordersCount").value(1))
                .andExpect(jsonPath("$.shippingCost").value(10))
                .andExpect(jsonPath("$.fee").value(5));

        then(orderDetailSalesReportService).should().getSalesReportByDateRange(START_DATE, END_DATE, SaleReportType.PRODUCT);
    }

    private ReportResponse createMockReportResponse() {
        ReportUnit reportUnit = new ReportUnit();
        reportUnit.setIdentifier("2023-01");
        reportUnit.setGrossSales(BigDecimal.valueOf(100));
        reportUnit.setNetSales(BigDecimal.valueOf(90));
        reportUnit.setOrdersCount(1);

        return ReportResponse.builder()
                .reports(List.of(reportUnit))
                .shippingCost(BigDecimal.valueOf(10))
                .fee(BigDecimal.valueOf(5))
                .build();
    }
}

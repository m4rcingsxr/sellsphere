package com.sellsphere.admin.report;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * REST controller for handling requests related to sales reports.
 * Provides endpoints for retrieving sales reports based on different periods or specific date
 * ranges.
 */
@RestController
public class ReportRestController {

    private final OrderSalesReportService salesReportService;
    private final OrderDetailSalesReportService orderDetailSalesReportService;

    // Maps strings representing report periods to functions that fetch the reports
    private final Map<String, BiFunction<SaleReportType, Boolean, List<ReportUnit>>> periodReportFetcherMap;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    @Autowired
    public ReportRestController(OrderSalesReportService salesReportService,
                                OrderDetailSalesReportService orderDetailSalesReportService) {
        this.salesReportService = salesReportService;
        this.orderDetailSalesReportService = orderDetailSalesReportService;
        this.periodReportFetcherMap = initializePeriodReportFetcherMap();
    }

    private Map<String, BiFunction<SaleReportType, Boolean, List<ReportUnit>>> initializePeriodReportFetcherMap() {
        return Map.of(
                "7_days", this::fetch7DaysReport,
                "28_days", this::fetch28DaysReport,
                "6_months", this::fetch6MonthsReport,
                "1_year", this::fetchYearReport
        );
    }

    /**
     * Endpoint to get report data by period.
     *
     * @param period The period string ("7_days", "28_days", "6_months", "1_year").
     * @return A ResponseEntity containing the list of report units or an error state.
     */
    @GetMapping("/reports/sales/{period}")
    public ResponseEntity<List<ReportUnit>> getReportDataByPeriod(@PathVariable String period) {
        return fetchReports(period, resolveTypeFromPeriod(period), false);
    }

    /**
     * Endpoint to get report data by specific start and end dates.
     *
     * @param start The start date in ISO format.
     * @param end The end date in ISO format.
     * @return A ResponseEntity containing the list of report units or an error state.
     */
    @GetMapping("/reports/sales/{start}/{end}")
    public ResponseEntity<List<ReportUnit>> getReportDataByDateRange(@PathVariable String start,
                                                                     @PathVariable String end) {
        return handleDateRangeFetch(start, end, false, "DAY");
    }

    /**
     * Endpoint to get detailed report data by report type and period.
     *
     * @param reportType The type of the report (CATEGORY, PRODUCT).
     * @param period The period string ("7_days", "28_days", "6_months", "1_year").
     * @return A ResponseEntity containing the list of report units or an error state.
     */
    @GetMapping("/reports/sales_by_group/{reportType}/{period}")
    public ResponseEntity<List<ReportUnit>> getReportDataByGroupAndPeriod(
            @PathVariable String reportType, @PathVariable String period) {
        return fetchReports(period, SaleReportType.valueOf(reportType.toUpperCase()), true);
    }

    /**
     * Endpoint to get detailed report data by report type over a specific date range.
     *
     * @param reportType The type of the report.
     * @param start The start date in ISO format.
     * @param end The end date in ISO format.
     * @return A ResponseEntity containing the list of report units or an error state.
     */
    @GetMapping("/reports/sales_by_group/{reportType}/{start}/{end}")
    public ResponseEntity<List<ReportUnit>> getReportDataByGroupAndDateRange(
            @PathVariable String reportType, @PathVariable String start, @PathVariable String end) {
        return handleDateRangeFetch(start, end, true, reportType);
    }


    private ResponseEntity<List<ReportUnit>> fetchReports(String period, SaleReportType type,
                                                          boolean isDetail) {
        return ResponseEntity.ok(periodReportFetcherMap.getOrDefault(period,
                                                                     (t, d) -> Collections.emptyList()
        ).apply(type, isDetail));
    }

    private ResponseEntity<List<ReportUnit>> handleDateRangeFetch(String start, String end,
                                                                  boolean isDetail,
                                                                  String reportType) {
        try {
            LocalDate startDate = LocalDate.parse(start, DATE_FORMATTER);
            LocalDate endDate = LocalDate.parse(end, DATE_FORMATTER);
            if (endDate.isBefore(startDate)) {
                return ResponseEntity.badRequest().build();
            }
            List<ReportUnit> reports = isDetail ?
                    orderDetailSalesReportService.getSalesReportByDateRange(startDate, endDate,
                                                                            SaleReportType.valueOf(
                                                                                    reportType)
                    )
                    : salesReportService.getSalesReportByDateRange(startDate, endDate,
                                                                   SaleReportType.valueOf(
                                                                           reportType)
            );
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private SaleReportType resolveTypeFromPeriod(String period) {
        return period.contains("months") || period.contains(
                "year") ? SaleReportType.MONTH : SaleReportType.DAY;
    }

    private List<ReportUnit> fetch7DaysReport(SaleReportType type, Boolean isDetail) {
        return isDetail ? orderDetailSalesReportService.getSalesReportFromLast7Days(
                type) : salesReportService.getSalesReportFromLast7Days(type);
    }

    private List<ReportUnit> fetch28DaysReport(SaleReportType type, Boolean isDetail) {
        return isDetail ? orderDetailSalesReportService.getSalesReportFromLast28Days(
                type) : salesReportService.getSalesReportFromLast28Days(type);
    }

    private List<ReportUnit> fetch6MonthsReport(SaleReportType type, Boolean isDetail) {
        return isDetail ? orderDetailSalesReportService.getSalesReportFromLast6Months(
                type) : salesReportService.getSalesReportFromLast6Months(type);
    }

    private List<ReportUnit> fetchYearReport(SaleReportType type, Boolean isDetail) {
        return isDetail ? orderDetailSalesReportService.getSalesReportFromLastYear(
                type) : salesReportService.getSalesReportFromLastYear(type);
    }
}
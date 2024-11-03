package com.sellsphere.admin.report;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * REST controller for handling requests related to sales reports.
 * Provides endpoints for retrieving sales reports based on different periods or specific date ranges.
 */
@RestController
public class ReportRestController {

    private final OrderSalesReportService salesReportService;
    private final OrderDetailSalesReportService orderDetailSalesReportService;

    // Maps report periods to functions that fetch the corresponding reports
    private final Map<String, BiFunction<SaleReportType, Boolean, ReportResponse>> periodReportFetcherMap;

    // Date format used for parsing date strings
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Constructor to inject the required services and initialize the periodReportFetcherMap.
     */
    @Autowired
    public ReportRestController(OrderSalesReportService salesReportService,
                                OrderDetailSalesReportService orderDetailSalesReportService) {
        this.salesReportService = salesReportService;
        this.orderDetailSalesReportService = orderDetailSalesReportService;
        this.periodReportFetcherMap = initializePeriodReportFetcherMap();
    }

    /**
     * Initializes a map that binds report period strings to their corresponding fetching methods.
     */
    private Map<String, BiFunction<SaleReportType, Boolean, ReportResponse>> initializePeriodReportFetcherMap() {
        return Map.of(
                "7_days", this::fetch7DaysReport,
                "28_days", this::fetch28DaysReport,
                "6_months", this::fetch6MonthsReport,
                "1_year", this::fetchYearReport
        );
    }

    /**
     * Endpoint to retrieve sales report data for a specific period.
     *
     * @param period The period string (e.g., "7_days", "28_days", "6_months", "1_year").
     * @return A ResponseEntity containing the report data for the specified period.
     */
    @GetMapping("/reports/sales/{period}")
    public ResponseEntity<ReportResponse> getReportDataByPeriod(@PathVariable String period) throws IOException {
        SaleReportType reportType = resolveTypeFromPeriod(period);
        ReportResponse report = fetchReports(period, reportType, false);

        return ResponseEntity.ok(report);
    }

    /**
     * Endpoint to retrieve sales report data for a specific date range.
     *
     * @param start The start date in ISO format.
     * @param end The end date in ISO format.
     * @return A ResponseEntity containing the report data for the specified date range.
     */
    @GetMapping("/reports/sales/{start}/{end}")
    public ResponseEntity<ReportResponse> getReportDataByDateRange(@PathVariable String start,
                                                                   @PathVariable String end) {
        return handleDateRangeFetch(start, end, false, "DAY");
    }

    /**
     * Endpoint to retrieve detailed sales report data based on report type (CATEGORY, PRODUCT) and period.
     *
     * @param reportType The type of the report (CATEGORY, PRODUCT).
     * @param period The period string ("7_days", "28_days", "6_months", "1_year").
     * @return A ResponseEntity containing the report data for the specified type and period.
     */
    @GetMapping("/reports/sales_by_group/{reportType}/{period}")
    public ResponseEntity<ReportResponse> getReportDataByGroupAndPeriod(@PathVariable String reportType,
                                                                        @PathVariable String period) {
        SaleReportType type = SaleReportType.valueOf(reportType.toUpperCase());
        ReportResponse report = fetchReports(period, type, true);
        return ResponseEntity.ok(report);
    }

    /**
     * Endpoint to retrieve detailed sales report data for a specific report type and date range.
     *
     * @param reportType The type of the report (CATEGORY, PRODUCT).
     * @param start The start date in ISO format.
     * @param end The end date in ISO format.
     * @return A ResponseEntity containing the report data for the specified type and date range.
     */
    @GetMapping("/reports/sales_by_group/{reportType}/{start}/{end}")
    public ResponseEntity<ReportResponse> getReportDataByGroupAndDateRange(@PathVariable String reportType, @PathVariable String start,
                                                                           @PathVariable String end) {
        return handleDateRangeFetch(start, end, true, reportType);
    }

    /**
     * Fetches the report based on the specified period and type.
     *
     * @param period The period string ("7_days", "28_days", "6_months", "1_year").
     * @param type The type of report (CATEGORY, PRODUCT).
     * @param isDetail Whether the report is detailed (grouped by category or product).
     * @return The fetched ReportResponse object.
     */
    private ReportResponse fetchReports(String period, SaleReportType type, boolean isDetail) {
        // Fetches the appropriate report based on the period; defaults to an empty response if not found.
        return periodReportFetcherMap.getOrDefault(period, (t, d) -> new ReportResponse())
                .apply(type, isDetail);
    }

    /**
     * Handles fetching reports based on a specific date range.
     *
     * @param start The start date in ISO format.
     * @param end The end date in ISO format.
     * @param isDetail Whether the report is detailed (grouped by category or product).
     * @param reportType The type of the report (e.g., CATEGORY, PRODUCT).
     * @return A ResponseEntity containing the report data for the date range.
     */
    private ResponseEntity<ReportResponse> handleDateRangeFetch(String start, String end,
                                                                boolean isDetail, String reportType) {
        try {
            // Parse the start and end dates
            LocalDate startDate = LocalDate.parse(start, DATE_FORMATTER);
            LocalDate endDate = LocalDate.parse(end, DATE_FORMATTER);

            // Validate that the end date is not before the start date
            if (endDate.isBefore(startDate)) {
                return ResponseEntity.badRequest().build();
            }

            // Fetch the report data based on whether it is a detailed report
            ReportResponse response = isDetail
                    ? orderDetailSalesReportService.getSalesReportByDateRange(startDate, endDate,
                                                                              SaleReportType.valueOf(reportType))
                    : salesReportService.getSalesReportByDateRange(startDate, endDate,
                                                                   SaleReportType.valueOf(reportType));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Resolves the report type (DAY or MONTH) based on the period string.
     *
     * @param period The period string.
     * @return The corresponding SaleReportType (DAY or MONTH).
     */
    private SaleReportType resolveTypeFromPeriod(String period) {
        return period.contains("months") || period.contains("year") ? SaleReportType.MONTH : SaleReportType.DAY;
    }

    // Fetch report data for the last 7 days
    private ReportResponse fetch7DaysReport(SaleReportType type, Boolean isDetail) {
        return isDetail ? orderDetailSalesReportService.getSalesReportFromLast7Days(type)
                : salesReportService.getSalesReportFromLast7Days(type);
    }

    // Fetch report data for the last 28 days
    private ReportResponse fetch28DaysReport(SaleReportType type, Boolean isDetail) {
        return isDetail ? orderDetailSalesReportService.getSalesReportFromLast28Days(type)
                : salesReportService.getSalesReportFromLast28Days(type);
    }

    // Fetch report data for the last 6 months
    private ReportResponse fetch6MonthsReport(SaleReportType type, Boolean isDetail) {
        return isDetail ? orderDetailSalesReportService.getSalesReportFromLast6Months(type)
                : salesReportService.getSalesReportFromLast6Months(type);
    }

    // Fetch report data for the last year
    private ReportResponse fetchYearReport(SaleReportType type, Boolean isDetail) {
        return isDetail ? orderDetailSalesReportService.getSalesReportFromLastYear(type)
                : salesReportService.getSalesReportFromLastYear(type);
    }
}

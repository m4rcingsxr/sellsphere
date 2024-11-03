package com.sellsphere.admin.report;

import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

/**
 * Abstract class serving as a base for report generation services.
 * Provides methods to fetch sales reports over various predefined periods.
 * It leverages the strategy pattern to delegate the implementation of fetching reports by date range to concrete subclasses.
 */
@RequiredArgsConstructor
public abstract class ReportManager {

    /**
     * Fetches sales reports for the last 7 days.
     *
     * @param reportType The type of sales report needed.
     * @return A list of report units representing sales data.
     */
    public ReportResponse getSalesReportFromLast7Days(SaleReportType reportType) {
        return getSalesReportFromLastXDays(7, reportType);
    }

    /**
     * Fetches sales reports for the last 28 days.
     *
     * @param reportType The type of sales report needed.
     * @return A list of report units representing sales data.
     */
    public ReportResponse getSalesReportFromLast28Days(SaleReportType reportType) {
        return getSalesReportFromLastXDays(28, reportType);
    }

    /**
     * Fetches sales reports for the last 6 months.
     *
     * @param reportType The type of sales report needed.
     * @return A list of report units representing sales data.
     */
    public ReportResponse getSalesReportFromLast6Months(SaleReportType reportType) {
        return getSalesReportFromLastXMonths(6, reportType);
    }

    /**
     * Fetches sales reports for the last 12 months (1 year).
     *
     * @param reportType The type of sales report needed.
     * @return A list of report units representing sales data.
     */
    public ReportResponse getSalesReportFromLastYear(SaleReportType reportType) {
        return getSalesReportFromLastXMonths(12, reportType);
    }

    private ReportResponse getSalesReportFromLastXDays(int days, SaleReportType reportType) {
        LocalDate endTime = LocalDate.now();
        LocalDate startTime = endTime.minusDays(days - 1);
        return getSalesReportByDateRange(startTime, endTime, reportType);
    }

    private ReportResponse getSalesReportFromLastXMonths(int months, SaleReportType reportType) {
        LocalDate endTime = LocalDate.now();
        LocalDate startTime = endTime.minusMonths(months - 1).withDayOfMonth(1);
        return getSalesReportByDateRange(startTime, endTime, reportType);
    }

    /**
     * Method that delegates the fetching of sales reports by date range to the subclass implementation.
     *
     * @param start The starting date of the report period.
     * @param end The ending date of the report period.
     * @param saleReportType The type of report being requested.
     * @return A list of report units.
     */
    public ReportResponse getSalesReportByDateRange(LocalDate start,
                                                      LocalDate end,
                                                      SaleReportType saleReportType) {
        return getSalesReportByDateRangeInternal(start, end, saleReportType);
    }

    /**
     * Abstract method to be implemented by subclasses to fetch sales reports based on a specified date range.
     * This method allows customization of report fetching logic in different concrete implementations.
     *
     * @param start The start date of the reporting period.
     * @param end The end date of the reporting period.
     * @param reportType The type of the report.
     * @return A list of report units detailing the sales in the given period.
     */
    protected abstract ReportResponse getSalesReportByDateRangeInternal(LocalDate start, LocalDate end, SaleReportType reportType);

}

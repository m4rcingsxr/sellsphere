package com.sellsphere.admin.report;

import com.sellsphere.admin.order.OrderRepository;
import com.sellsphere.common.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.sellsphere.admin.report.SalesReportHelper.calculateFee;
import static com.sellsphere.admin.report.SalesReportHelper.calculateShippingCost;
import static java.util.stream.Collectors.groupingBy;

/**
 * Service class responsible for generating sales reports based on order data.
 * This service handles report generation for sales data, grouping it by either day or month.
 * It extends the {@link ReportManager} class to provide a specialized implementation for
 * handling {@link Order} entities and calculating relevant sales metrics.
 *
 * <p>The service works in conjunction with the {@link OrderRepository} to retrieve
 * order data within a specific date range, supporting DAY and MONTH report types.</p>
 *
 * <p>Each sales report includes key financial metrics such as gross sales, net sales,
 * shipping costs, and platform fees.</p>
 */
@RequiredArgsConstructor
@Service
public class OrderSalesReportService extends ReportManager {

    private final OrderRepository orderRepository;

    /**
     * Generates a sales report for the specified date range and report type.
     *
     * <p>The report data is aggregated by the report type (DAY or MONTH) and includes
     * shipping costs and platform fees in the final report.</p>
     *
     * @param start The start date of the report period.
     * @param end The end date of the report period.
     * @param reportType The type of the report, determining how the data is grouped (e.g., by day or by month).
     * @return A {@link ReportResponse} object containing the aggregated report data.
     * @throws IllegalStateException if an unsupported report type is provided.
     */
    @Override
    protected ReportResponse getSalesReportByDateRangeInternal(
            LocalDate start,
            LocalDate end,
            SaleReportType reportType) {
        return createReportData(start, end, reportType);
    }

    /**
     * Creates a sales report by fetching order data between the specified start and end dates.
     * The report includes aggregated financial data such as gross sales, net sales, shipping costs, and platform fees.
     *
     * @param start The start date for the report data.
     * @param end The end date for the report data.
     * @param reportType The type of report (DAY or MONTH), determining how the data is grouped.
     * @return A {@link ReportResponse} containing the detailed sales report data.
     */
    private ReportResponse createReportData(LocalDate start, LocalDate end, SaleReportType reportType) {
        // Fetch orders from the repository between the start and end dates
        List<Order> ordersBetweenDates = orderRepository
                .findByOrderTimeBetween(start.atStartOfDay(), end.atTime(23, 59, 59));

        // Create report units based on the fetched orders and report type
        List<ReportUnit> reportUnits = createReportUnits(ordersBetweenDates, start, end, reportType);

        // Calculate shipping cost and platform fee for the entire set of orders
        BigDecimal shippingCost = calculateShippingCost(ordersBetweenDates.stream());
        BigDecimal fee = calculateFee(ordersBetweenDates.stream());

        // Build and return the complete report response
        return ReportResponse.builder()
                .reports(reportUnits)
                .shippingCost(shippingCost)
                .fee(fee)
                .build();
    }

    /**
     * Creates report units by grouping the order data based on the specified report type (DAY or MONTH).
     *
     * <p>The report units are created by grouping orders either by day or by month. Each report unit contains
     * aggregated sales data for a specific day or month.</p>
     *
     * @param ordersBetweenDates The list of orders between the specified dates.
     * @param start The start date for generating the report.
     * @param end The end date for generating the report.
     * @param reportType The type of report (DAY or MONTH), determining how the data is grouped.
     * @return A list of {@link ReportUnit} objects, each containing aggregated sales data for a specific period.
     * @throws IllegalStateException if an unsupported report type is provided.
     */
    private List<ReportUnit> createReportUnits(List<Order> ordersBetweenDates, LocalDate start, LocalDate end, SaleReportType reportType) {

        // Declare variables for grouping orders and formatting dates
        Map<LocalDate, List<Order>> orderMap;
        Stream<LocalDate> dateStream;
        DateTimeFormatter formatter;

        switch (reportType) {
            case DAY -> {
                // Group orders by day and set up a date formatter for daily reports
                orderMap = ordersBetweenDates.stream().collect(groupingBy(order -> order.getOrderTime().toLocalDate()));
                formatter = DateTimeFormatter.ISO_LOCAL_DATE;
                // Generate a stream of dates between start and end, inclusive
                dateStream = start.datesUntil(end.plusDays(1));
            }
            case MONTH -> {
                // Group orders by month and set up a date formatter for monthly reports
                orderMap = ordersBetweenDates.stream().collect(groupingBy(order -> YearMonth.from(order.getOrderTime()).atDay(1)));
                formatter = DateTimeFormatter.ofPattern("yyyy-MM");
                // Generate a stream of the first days of each month between start and end
                dateStream = start.withDayOfMonth(1).datesUntil(end, Period.ofMonths(1));
            }
            default -> throw new IllegalStateException("Only DAY and MONTH SaleReportType supported.");
        }

        // Create a report unit for each date in the date stream
        return dateStream.map(date -> createReportUnit(date.format(formatter), orderMap.getOrDefault(date, List.of()))).toList();
    }

    /**
     * Constructs a single {@link ReportUnit} by aggregating sales data for a specific date or month.
     *
     * <p>This method aggregates gross sales, net sales, and order count for the given list of orders
     * and associates the data with a specific date or month, which is passed as a formatted string.</p>
     *
     * @param dateString The formatted date string representing the specific day or month for this report unit.
     * @param orders The list of {@link Order} entities for the given date or month.
     * @return A constructed {@link ReportUnit} containing the aggregated sales data.
     */
    private ReportUnit createReportUnit(String dateString, List<Order> orders) {
        // Create a new report unit and assign the identifier (formatted date)
        ReportUnit reportUnit = new ReportUnit();
        reportUnit.setIdentifier(dateString);

        // If there are no orders for this date, return an empty report unit
        if (orders.isEmpty()) {
            return reportUnit;
        }

        // Aggregate gross sales, net sales, and order count for each order
        for (Order order : orders) {
            reportUnit.addGrossSales(order.getTransaction().getDisplayAmount());
            reportUnit.addNetSales(order.getTransaction().getDisplayNet());
            reportUnit.increaseOrdersCount();
        }

        return reportUnit;
    }
}

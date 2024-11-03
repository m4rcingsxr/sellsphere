package com.sellsphere.admin.report;

import com.sellsphere.admin.order.OrderDetailRepository;
import com.sellsphere.common.entity.OrderDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static com.sellsphere.admin.report.SalesReportHelper.calculateFee;
import static com.sellsphere.admin.report.SalesReportHelper.calculateShippingCost;
import static java.util.stream.Collectors.groupingBy;

/**
 * Service class responsible for generating detailed sales reports based on order details.
 * This service handles report generation for different types of sales data, such as sales by product or category.
 * It extends the {@link ReportManager} class, providing a specialized implementation for handling detailed
 * sales data from {@link OrderDetail} entities.
 *
 * <p>This service works in conjunction with the {@link OrderDetailRepository} to fetch and aggregate order
 * details between specified dates, supporting report types like CATEGORY and PRODUCT.</p>
 *
 * <p>Each sales report includes key financial metrics such as gross sales, net sales, shipping costs, and platform fees.</p>
 */
@RequiredArgsConstructor
@Service
public class OrderDetailSalesReportService extends ReportManager {

    // Dependency on the OrderDetailRepository to interact with the database and fetch order details.
    private final OrderDetailRepository orderDetailRepository;

    /**
     * Fetches and generates sales reports for a specified date range and report type.
     * It aggregates the sales data, calculates key metrics (like fees and shipping costs),
     * and groups the data based on the provided report type (e.g., CATEGORY or PRODUCT).
     *
     * @param start The start date of the report period.
     * @param end The end date of the report period.
     * @param reportType The type of the report, which determines how the data is grouped (e.g., by category or product).
     * @return A {@link ReportResponse} containing the generated report data, including grouped sales units, fees, and shipping costs.
     * @throws IllegalStateException if an unsupported report type is provided (only CATEGORY and PRODUCT are supported).
     */
    @Override
    protected ReportResponse getSalesReportByDateRangeInternal(
            LocalDate start,
            LocalDate end,
            SaleReportType reportType) {
        return createReportData(start, end, reportType);
    }

    /**
     * Creates the report data by fetching order details for the specified date range and report type,
     * and then aggregating the data into report units.
     *
     * <p>It also calculates the overall shipping cost and platform fee for the provided data.</p>
     *
     * @param start The start date for the report data.
     * @param end The end date for the report data.
     * @param reportType The type of report (CATEGORY or PRODUCT) that dictates how the data will be grouped.
     * @return A {@link ReportResponse} containing a list of report units, shipping cost, and platform fee.
     */
    private ReportResponse createReportData(LocalDate start, LocalDate end, SaleReportType reportType) {

        List<OrderDetail> orderDetailsBetweenDates = fetchOrderDetails(start, end, reportType);

        // Create individual report units by grouping order details based on the report type.
        List<ReportUnit> reportUnits = createReportUnits(orderDetailsBetweenDates, reportType);

        // Calculate shipping costs and platform fees, taking into account the distinct orders.
        BigDecimal shippingCost = calculateShippingCost(orderDetailsBetweenDates.stream().map(OrderDetail::getOrder).distinct());
        BigDecimal fee = calculateFee(orderDetailsBetweenDates.stream().map(OrderDetail::getOrder).distinct());

        // Return the complete report response with all relevant data.
        return ReportResponse.builder()
                .reports(reportUnits)
                .fee(fee)
                .shippingCost(shippingCost)
                .build();
    }

    /**
     * Fetches order details between the provided start and end dates based on the report type.
     *
     * <p>For the CATEGORY report type, it fetches details including category names. For the PRODUCT type, it fetches
     * details including product names.</p>
     *
     * @param start The start date for fetching order details.
     * @param end The end date for fetching order details.
     * @param reportType The type of report (CATEGORY or PRODUCT), which influences how the data is fetched.
     * @return A list of {@link OrderDetail} objects matching the date range and report type.
     * @throws IllegalStateException if an unsupported report type is provided.
     */
    private List<OrderDetail> fetchOrderDetails(LocalDate start, LocalDate end, SaleReportType reportType) {
        List<OrderDetail> ordersBetweenDates;

        switch(reportType) {
            case CATEGORY -> ordersBetweenDates = orderDetailRepository.findAllWithCategoryNameAndTimeBetween(
                    start.atStartOfDay(), end.atTime(23, 59, 59));
            case PRODUCT -> ordersBetweenDates = orderDetailRepository.findAllWithProductNameAndTimeBetween(
                    start.atStartOfDay(), end.atTime(23, 59, 59));
            default -> throw new IllegalStateException("Only PRODUCT and CATEGORY SaleReportType are supported.");
        }
        return ordersBetweenDates;
    }


    /**
     * Creates report units by grouping order details based on the provided report type.
     *
     * @param orderDetailsBetweenDates The list of order details to be grouped.
     * @param reportType The type of report (CATEGORY or PRODUCT) that influences the grouping.
     * @return A list of {@link ReportUnit} objects, each representing a group of order details.
     */
    private List<ReportUnit> createReportUnits(List<OrderDetail> orderDetailsBetweenDates, SaleReportType reportType) {
        Map<String, List<OrderDetail>> orderDetailGroup = groupOrderDetails(reportType, orderDetailsBetweenDates);
        return orderDetailGroup.entrySet().stream()
                .map(this::createReportUnit)
                .toList();
    }

    /**
     * Groups the order details based on the report type.
     *
     * <p>For CATEGORY reports, the data is grouped by category name. For PRODUCT reports, the data is grouped by product name.</p>
     *
     * @param reportType The type of report (CATEGORY or PRODUCT) which determines the grouping criterion.
     * @param orderDetailsBetweenDates The list of order details to group.
     * @return A map where the keys are category or product names, and the values are lists of corresponding {@link OrderDetail} objects.
     */
    private Map<String, List<OrderDetail>> groupOrderDetails(SaleReportType reportType, List<OrderDetail> orderDetailsBetweenDates) {
        if(reportType.equals(SaleReportType.CATEGORY)) {
            return orderDetailsBetweenDates.stream()
                    .collect(groupingBy(orderDetail -> orderDetail.getProduct().getCategory().getName()));
        } else {
            return orderDetailsBetweenDates.stream()
                    .collect(groupingBy(orderDetail -> orderDetail.getProduct().getName()));
        }
    }

    /**
     * Constructs a single {@link ReportUnit} from a group of order details.
     *
     * <p>Each {@link ReportUnit} contains aggregated sales data (gross and net sales) and product count,
     * which are calculated by iterating over the group of {@link OrderDetail} objects.</p>
     *
     * @param reportUnitEntry A map entry where the key is the group identifier (category or product name) and
     *                        the value is a list of {@link OrderDetail} objects in that group.
     * @return A constructed {@link ReportUnit} with aggregated data.
     */
    private ReportUnit createReportUnit(Map.Entry<String, List<OrderDetail>> reportUnitEntry) {
        ReportUnit reportUnit = new ReportUnit();
        reportUnit.setIdentifier(reportUnitEntry.getKey());

        for (OrderDetail detail : reportUnitEntry.getValue()) {
            reportUnit.addGrossSales(detail.getSubtotal());
            reportUnit.addNetSales(detail.getSubtotal());
            reportUnit.increaseProductsCountBy(detail.getQuantity());
        }

        return reportUnit;
    }
}

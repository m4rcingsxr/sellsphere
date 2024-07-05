package com.sellsphere.admin.report;

import com.sellsphere.admin.order.OrderDetailRepository;
import com.sellsphere.common.entity.OrderDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service class for generating detailed sales reports based on order details.
 * Extends {@link ReportManager} to provide specific implementations for handling detailed order data.
 */
@RequiredArgsConstructor
@Service
public class OrderDetailSalesReportService extends ReportManager {

    private final OrderDetailRepository orderDetailRepository;

    /**
     * Generates detailed sales reports by fetching order details from the database for the specified date range and report type.
     *
     * @param start The start date of the report period.
     * @param end The end date of the report period.
     * @param reportType The type of the report (e.g., CATEGORY, PRODUCT), determines the granularity of the report.
     * @return A list of {@link ReportUnit} containing detailed sales data.
     * @throws IllegalStateException if an unsupported report type is provided.
     */
    @Override
    protected List<ReportUnit> getSalesReportByDateRangeInternal(
            LocalDate start,
            LocalDate end,
            SaleReportType reportType) {

        List<OrderDetail> ordersBetweenDates;

        switch(reportType) {
            case CATEGORY -> ordersBetweenDates = orderDetailRepository.findAllWithCategoryNameAndTimeBetween(start.atStartOfDay(), end.atTime(23,59,59));
            case PRODUCT -> ordersBetweenDates = orderDetailRepository.findAllWithProductNameAndTimeBetween(start.atStartOfDay(), end.atTime(23,59,59));
            default -> throw new IllegalStateException("Only PRODUCT, CATEGORY SaleReportType supported.");
        }

        return createReportData(ordersBetweenDates, reportType);
    }

    // Aggregates order detail data into report units based on the specified report type.
    private List<ReportUnit> createReportData(List<OrderDetail> orderDetails,
                                              SaleReportType reportType) {
        Map<String, List<OrderDetail>> orderMap;

        // Group order details by category or product name based on the report type.
        if(reportType.equals(SaleReportType.CATEGORY)) {
            orderMap = orderDetails.stream()
                    .collect(Collectors.groupingBy(orderDetail -> orderDetail.getProduct().getCategory().getName()));
        } else {
            orderMap = orderDetails.stream()
                    .collect(Collectors.groupingBy(orderDetail -> orderDetail.getProduct().getName()));
        }

        // Create report units from the grouped data.
        return orderMap.entrySet().stream().map((entry) -> createReportUnit(entry.getKey(), entry.getValue())).toList();
    }

    // Constructs a single report unit from a group of order details.
    private ReportUnit createReportUnit(String category, List<OrderDetail> orders) {
        ReportUnit reportUnit = new ReportUnit();
        reportUnit.setIdentifier(category);
        orders.forEach(orderDetail -> {
            reportUnit.addGrossSales(orderDetail.getSubtotal());
            reportUnit.addNetSales(orderDetail.getSubtotal().subtract(orderDetail.getProductCost()));
            reportUnit.increaseProductsCount();
        });

        return reportUnit;
    }

}

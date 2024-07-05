package com.sellsphere.admin.report;

import com.sellsphere.admin.order.OrderRepository;
import com.sellsphere.common.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service class extending ReportManager to provide specific implementations for fetching sales reports from order data.
 * This class utilizes an OrderRepository to access order data stored in the database.
 */
@RequiredArgsConstructor
@Service
public class OrderSalesReportService extends ReportManager {

    private final OrderRepository orderRepository;

    /**
     * Fetches sales reports for a given date range and report type by querying the order repository.
     *
     * @param start The starting date of the report period.
     * @param end The ending date of the report period.
     * @param reportType The type of the report, affecting the grouping of the data.
     * @return A list of report units containing aggregated sales data.
     */
    @Override
    protected List<ReportUnit> getSalesReportByDateRangeInternal(
            LocalDate start,
            LocalDate end,
            SaleReportType reportType) {

        List<Order> ordersBetweenDates = orderRepository.findByOrderTimeBetween(
                start.atStartOfDay(), end.atTime(23, 59, 59));

        return createReportData(start, end, ordersBetweenDates, reportType);
    }

    private List<ReportUnit> createReportData(LocalDate start, LocalDate end,
                                              List<Order> orders,
                                              SaleReportType reportType) {
        Map<LocalDate, List<Order>> orderMap;
        Stream<LocalDate> dateStream;
        DateTimeFormatter formatter;

        switch (reportType) {
            case DAY -> {
                orderMap = orders.stream()
                        .collect(Collectors.groupingBy(
                                order -> order.getOrderTime().toLocalDate()));
                formatter = DateTimeFormatter.ISO_LOCAL_DATE;
                dateStream = start.datesUntil(end.plusDays(1));
            }
            case MONTH -> {
                orderMap = orders.stream()
                        .collect(Collectors.groupingBy(order -> YearMonth.from(order.getOrderTime()).atDay(1)));
                formatter = DateTimeFormatter.ofPattern("yyyy-MM");
                dateStream = start.withDayOfMonth(1).datesUntil(end, Period.ofMonths(1));
            }
            default -> throw new IllegalStateException("Only DAY,MONTH SaleReportType supported.");

        }

        return dateStream
                .map(date -> createReportUnit(date, orderMap.getOrDefault(date, List.of()
                ), formatter))
                .toList();
    }

    private ReportUnit createReportUnit(LocalDate date, List<Order> orders,
                                        DateTimeFormatter formatter) {
        String dateString = date.format(formatter);
        ReportUnit reportUnit = new ReportUnit();
        reportUnit.setIdentifier(dateString);
        orders.forEach(order -> {
            reportUnit.addGrossSales(order.getTransaction().getDisplayAmount());
            reportUnit.addNetSales(order.getTransaction().getDisplayNet());
            reportUnit.increaseOrdersCount();
        });

        return reportUnit;
    }

}

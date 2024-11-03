package com.sellsphere.admin.report;

import com.sellsphere.common.entity.Order;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.util.stream.Stream;

@UtilityClass
public class SalesReportHelper {

    public static BigDecimal calculateFee(Stream<Order> orders) {
        return orders
                .map(order -> order.getTransaction().getDisplayFee())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static BigDecimal calculateShippingCost(Stream<Order> orders) {
        return orders
                .map(order -> order.getTransaction().getDisplayShippingAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}

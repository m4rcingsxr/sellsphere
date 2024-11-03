package com.sellsphere.common.entity;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public enum OrderStatus {
    NEW("New order received and awaiting processing"),
    PAID("Order has been paid for"),
    PROCESSING("Order is being processed"),
    PACKAGED("Order items have been packaged"),
    PICKED("Order items have been picked"),
    SHIPPING("Order is out for shipping"),
    DELIVERED("Order has been successfully delivered"),
    RETURN_REQUESTED("Customer sent request to return purchase"),
    RETURNED("Order has been returned"),
    REFUNDED("Order has been refunded"),
    CANCELLED("Order has been cancelled");

    private final String note;
    OrderStatus(String note) {
        this.note = note;
    }

    public static Map<String, String> getOrderStatusMap() {
        return Arrays.stream(OrderStatus.values())
                .collect(Collectors.toMap(Enum::name, OrderStatus::getNote));
    }
}
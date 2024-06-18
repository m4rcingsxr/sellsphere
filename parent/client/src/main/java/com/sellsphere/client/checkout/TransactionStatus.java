package com.sellsphere.client.checkout;

public enum TransactionStatus {

    CANCELED,
    PROCESSING,
    REQUIRES_ACTION,
    REQUIRES_CAPTURE,
    REQUIRES_CONFIRMATION,
    REQUIRES_PAYMENT_METHOD,
    SUCCEEDED

}
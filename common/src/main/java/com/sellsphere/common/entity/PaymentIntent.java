package com.sellsphere.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "payment_intents")
public class PaymentIntent extends IdentifiedEntity {

    // Unique identifier for the PaymentIntent in Stripe
    @Column(name = "stripe_id", nullable = false)
    private String stripeId;

    // Amount intended to be collected by this PaymentIntent in the smallest currency unit
    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "shipping_amount", nullable = false)
    private Long shippingAmount;

    @Column(name = "shipping_tax", nullable = false)
    private Long shippingTax;

    @Column(name = "tax_amount", nullable = false)
    private Long taxAmount;

    @Column(name = "exchange_rate", precision = 18, scale = 8)
    private BigDecimal exchangeRate;

    @ManyToOne
    @JoinColumn(name = "base_currency_id", nullable = false)
    private Currency baseCurrency;

    @ManyToOne
    @JoinColumn(name = "address_id", nullable = false)
    private Address shippingAddress;

    // Three-letter ISO currency code, in lowercase
    @ManyToOne
    @JoinColumn(name = "target_currency_id", nullable = false)
    private Currency targetCurrency;

    // ID of the Customer this PaymentIntent belongs to, if one exists
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    // Status of this PaymentIntent
    @Column(name = "status", nullable = false)
    private String status;

    // Time at which the object was created
    @Column(name = "created", nullable = false)
    private Long created;

    // Time at which the PaymentIntent was canceled, if it was canceled
    @Column(name = "canceled_at")
    private Long canceledAt;

    // Reason for cancellation of this PaymentIntent
    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @ManyToOne
    @JoinColumn(name = "payment_method_id")
    private PaymentMethod paymentMethod;

    @OneToOne(mappedBy = "paymentIntent")
    private Charge charge;

    @ManyToOne
    @JoinColumn(name = "courier_id", nullable = false)
    private Courier courier;

    @Transient
    public BigDecimal getDisplayShippingAmount() {
        return convertToDisplayAmount(shippingAmount, targetCurrency.getUnitAmount().longValue(), RoundingMode.CEILING);
    }

    @Transient
    public BigDecimal getDisplayAmount() {
        return convertToDisplayAmount(amount, targetCurrency.getUnitAmount().longValue(), RoundingMode.CEILING);
    }

    @Transient
    public BigDecimal getDisplayFee() {
        long finalFee = calculateTotalFee();
        return convertToDisplayAmount(finalFee, targetCurrency.getUnitAmount().longValue(), RoundingMode.CEILING);
    }

    @Transient
    public BigDecimal getDisplayNet() {
        long finalNet = calculateTotalNet();
        return convertToDisplayAmount(finalNet, targetCurrency.getUnitAmount().longValue(), RoundingMode.CEILING);
    }

    @Transient
    public BigDecimal getDisplayTax() {
        return convertToDisplayAmount(taxAmount, targetCurrency.getUnitAmount().longValue(), RoundingMode.CEILING);
    }

    @Transient
    public boolean hasRefunds() {
        return charge.getRefunds() != null && !charge.getRefunds().isEmpty();
    }

    @Transient
    public String getDisplayRefunded() {
        if (!hasRefunds()) {
            return null;
        }

        BigDecimal totalRefund = getTotalRefunded();
        long settlementCurrencyTotalAmount = calculateSettlementCurrencyTotalAmount();

        String settlementCurrency = charge.getBalanceTransaction().getCurrency().getCode();
        String presentmentCurrency = targetCurrency.getCode();

        if (settlementCurrency.equals(presentmentCurrency)) {
            return formatAmount(totalRefund, settlementCurrency);
        } else {
            BigDecimal convertedAmount = convertToSettlementCurrency(settlementCurrencyTotalAmount, charge.getBalanceTransaction().getCurrency().getUnitAmount());
            return formatAmount(totalRefund, presentmentCurrency) + " -> " + formatAmount(convertedAmount, settlementCurrency);
        }
    }


    private BigDecimal convertToDisplayAmount(long amount, long unitAmount, RoundingMode roundingMode) {
        return BigDecimal.valueOf(amount)
                .divide(BigDecimal.valueOf(unitAmount), 2, roundingMode);
    }

    private long calculateTotalFee() {
        if (charge == null) {
            return 0L;
        }

        long finalFee = charge.getBalanceTransaction().getFee();
        if (hasRefunds()) {
            finalFee += charge.getRefunds().stream()
                    .mapToLong(refund -> refund.getBalanceTransaction().getFee())
                    .sum();
        }

        return finalFee;
    }

    private long calculateTotalNet() {
        if (charge == null) {
            return 0L;
        }

        long finalNet = charge.getBalanceTransaction().getNet();
        if (hasRefunds()) {
            finalNet += charge.getRefunds().stream()
                    .mapToLong(refund -> refund.getBalanceTransaction().getNet())
                    .sum();
        }

        return finalNet;
    }

    private long calculateSettlementCurrencyTotalAmount() {
        return charge.getRefunds().stream()
                .mapToLong(refund -> refund.getBalanceTransaction().getAmount())
                .sum();
    }

    private BigDecimal convertToSettlementCurrency(long amount, BigDecimal unitAmount) {
        return BigDecimal.valueOf(amount)
                .divide(unitAmount, 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(-1));
    }

    private String formatAmount(BigDecimal amount, String currency) {
        return amount.toPlainString() + " " + currency.toUpperCase();
    }

    private BigDecimal getTotalRefunded() {
        return convertToDisplayAmount(charge.getAmountRefunded(), targetCurrency.getUnitAmount().longValue(), RoundingMode.CEILING);
    }

}


package com.sellsphere.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

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

    @Column(name = "receipt_url")
    private String receiptUrl;

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

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.DETACH})
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

    @OneToOne(mappedBy = "paymentIntent", cascade = CascadeType.ALL)
    private Charge charge;

    @ManyToOne
    @JoinColumn(name = "courier_id", nullable = false)
    private Courier courier;

    @Transient
    public String getDisplayStatus() {
        if(hasRefunds()) {
            if(charge.getRefunded()) {
                return "refunded";
            } else {
                return "Partial Refund";
            }
        } else {
            return status;
        }
    }

    @Transient
    public BigDecimal getDisplayShippingAmount() {
        return MoneyUtil.convertToDisplayPrice(shippingAmount, targetCurrency.getUnitAmount());
    }

    @Transient
    public String getDisplayShippingString() {
        return MoneyUtil.formatAmount(getDisplayShippingAmount(), targetCurrency.getCode());
    }

    @Transient
    public BigDecimal getDisplayAmount() {
        return MoneyUtil.convertToDisplayPrice(amount, targetCurrency.getUnitAmount());
    }

    @Transient
    public String getDisplayAmountString() {
        return MoneyUtil.formatAmount(getDisplayAmount(), targetCurrency.getCode());
    }

    @Transient
    public BigDecimal getDisplayFee() {
        long finalFee = calculateTotalFee();
        return MoneyUtil.convertToDisplayPrice(finalFee, targetCurrency.getUnitAmount());
    }

    @Transient
    public String getDisplayFeeString() {
        return MoneyUtil.formatAmount(getDisplayFee(), targetCurrency.getCode());
    }

    @Transient
    public BigDecimal getDisplayNet() {
        long finalNet = calculateTotalNet();
        return MoneyUtil.convertToDisplayPrice(finalNet, targetCurrency.getUnitAmount());
    }

    @Transient
    public String getDisplayNetString() {
       return MoneyUtil.formatAmount(getDisplayAmount(), targetCurrency.getCode());
    }

    @Transient
    public BigDecimal getDisplayTax() {
        return MoneyUtil.convertToDisplayPrice(taxAmount, targetCurrency.getUnitAmount());
    }

    @Transient
    public String getDisplayTaxString() {
        return MoneyUtil.formatAmount(getDisplayTax(), targetCurrency.getCode());
    }

    @Transient
    public boolean hasRefunds() {
        return charge.getRefunds() != null && !charge.getRefunds().isEmpty();
    }

    @Transient
    public String getDisplayRefundedString() {
        if (!hasRefunds()) {
            return "0";
        }
        BigDecimal totalRefund = getTotalRefunded();
        return MoneyUtil.formatAmount(totalRefund, targetCurrency.getCode());
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

    public BigDecimal getTotalRefunded() {
        return MoneyUtil.convertToDisplayPrice(charge.getAmountRefunded(),
                                               targetCurrency.getUnitAmount());
    }

}


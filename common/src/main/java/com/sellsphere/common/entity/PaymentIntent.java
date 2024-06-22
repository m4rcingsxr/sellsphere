package com.sellsphere.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "payment_intents")
public class PaymentIntent extends IdentifiedEntity {

    // Unique identifier for the PaymentIntent in Stripe
    @Column(name = "stripe_id")
    private String stripeId;

    // Amount intended to be collected by this PaymentIntent in the smallest currency unit
    @Column(name = "amount")
    private Long amount;

    @Column(name = "shipping_amount")
    private Long shippingAmount;

    @Column(name = "shipping_tax")
    private Long shippingTax;

    @Column(name = "tax_amount")
    private Long taxAmount;

    @Column(name = "exchange_rate")
    private BigDecimal exchangeRate;

    @ManyToOne
    @JoinColumn(name = "base_currency_id")
    private Currency baseCurrency;

    @ManyToOne
    @JoinColumn(name = "address_id")
    private Address shippingAddress;

    // Three-letter ISO currency code, in lowercase
    @ManyToOne
    @JoinColumn(name = "target_currency_id")
    private Currency targetCurrency;

    // ID of the Customer this PaymentIntent belongs to, if one exists
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    // Status of this PaymentIntent
    @Column(name = "status")
    private String status;

    // Time at which the object was created
    @Column(name = "created")
    private Long created;

    // Time at which the PaymentIntent was canceled, if it was canceled
    @Column(name = "canceled_at")
    private Long canceledAt;

    // Reason for cancellation of this PaymentIntent
    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Column(name = "client_secret")
    private String clientSecret;

    @ManyToOne
    @JoinColumn(name = "payment_method_id")
    private PaymentMethod paymentMethod;

    @OneToOne(mappedBy = "paymentIntent")
    private Charge charge;

    // tax transaction id and selected rate courier id
    @ManyToOne
    @JoinColumn(name = "courier_id")
    private Courier courier;

    @Transient
    public BigDecimal getDisplayAmount() {
        long unitAmount = targetCurrency.getUnitAmount().longValue();

        return BigDecimal.valueOf(amount).divide(BigDecimal.valueOf(unitAmount)).setScale(2,
                                                                                          RoundingMode.CEILING
        );
    }

    @Transient
    public BigDecimal getDisplayRefunded() {
        long unitAmount = targetCurrency.getUnitAmount().longValue();

        return BigDecimal.valueOf(charge.getAmountRefunded())
                .divide(BigDecimal.valueOf(unitAmount))
                .setScale(2, RoundingMode.CEILING);
    }

    @Transient
    public BigDecimal getDisplayFee() {
        Long finalFee = 0L;

        if (this.charge != null) {
            finalFee = charge.getBalanceTransaction().getFee();

            if (this.charge.getRefunds() != null && !this.charge.getRefunds().isEmpty()) {
                for (Refund refund : this.charge.getRefunds()) {
                    Long fee = refund.getBalanceTransaction().getFee();
                    finalFee += fee;
                }
            }
        }

        long unitAmount = targetCurrency.getUnitAmount().longValue();

        return BigDecimal.valueOf(finalFee)
                .divide(BigDecimal.valueOf(unitAmount))
                .setScale(2, RoundingMode.CEILING);
    }

    @Transient
    public BigDecimal getDisplayNet() {
        Long finalNet = 0L;

        if (this.charge != null) {
            finalNet = charge.getBalanceTransaction().getNet();

            if (this.charge.getRefunds() != null && !this.charge.getRefunds().isEmpty()) {

                for (Refund refund : this.charge.getRefunds()) {
                    Long net = refund.getBalanceTransaction().getNet();
                    finalNet += net;
                }
            }
        }

        long unitAmount = targetCurrency.getUnitAmount().longValue();

        return BigDecimal.valueOf(finalNet)
                .divide(BigDecimal.valueOf(unitAmount))
                .setScale(2, RoundingMode.CEILING);
    }

}

package com.sellsphere.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    @Column(name = "application_fee_amount")
    private Long applicationFeeAmount;

    // Three-letter ISO currency code, in lowercase
    @ManyToOne
    @JoinColumn(name = "currency_id")
    private Currency currency;

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

    @OrderBy("created asc")
    @OneToMany(mappedBy = "paymentIntent")
    private List<Refund> refunds;

    @OneToMany(mappedBy = "paymentIntent")
    private List<Charge> charges;

    @ManyToOne
    @JoinColumn(name = "payment_method_id")
    private PaymentMethod paymentMethod;

    // get metadata - retrieve calculation id or transaction tax - to get tax info
    @OneToMany
    @JoinColumn(name = "metadata_id")
    private List<MetadataEntry> metadata;

    @Transient
    public BigDecimal getDisplayAmount() {
        Long unitAmount = currency.getUnitAmount();

        return BigDecimal.valueOf(amount).divide(BigDecimal.valueOf(unitAmount)).setScale(2,
                                                                                          RoundingMode.CEILING
        );
    }

    @Transient
    public boolean hasRefunds() {
        return refunds != null && !refunds.isEmpty();
    }

    @Transient
    public boolean isPartial() {
        Long refundAmount = sumRefundAmount();
        return refunds != null && refundAmount > 0 && refundAmount < amount;
    }

    @Transient
    public boolean isRefunded() {
        return sumRefundAmount().equals(amount);
    }


    private Long sumRefundAmount() {
        return refunds.stream()
                .map(Refund::getAmount)
                .reduce(0L, Long::sum);
    }

}

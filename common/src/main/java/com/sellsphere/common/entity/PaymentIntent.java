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

    // Status of this PaymentIntent (todo: check status on refund - do i need to retrieve refund object and check status to update it??)
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

    // get metadata - retrieve calculation id or transaction tax - to get tax info

}

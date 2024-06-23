package com.sellsphere.client.checkout;

import com.sellsphere.common.entity.PaymentIntent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentIntentRepository extends JpaRepository<PaymentIntent, Integer> {

    Optional<PaymentIntent> findByStripeId(String stripeId);
}

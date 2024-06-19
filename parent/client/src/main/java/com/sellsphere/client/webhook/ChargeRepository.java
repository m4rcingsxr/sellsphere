package com.sellsphere.client.webhook;

import com.sellsphere.common.entity.Charge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChargeRepository extends JpaRepository<Charge, Integer> {
    Optional<Charge> findByStripeId(String id);
}

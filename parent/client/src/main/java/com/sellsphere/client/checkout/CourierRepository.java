package com.sellsphere.client.checkout;

import com.sellsphere.common.entity.Courier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourierRepository extends JpaRepository<Courier, Integer> {

    Optional<Courier> findByName(String courierName);

}
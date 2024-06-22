package com.sellsphere.client.checkout;

import com.sellsphere.common.entity.Courier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourierRepository extends JpaRepository<Courier, Integer> {
}
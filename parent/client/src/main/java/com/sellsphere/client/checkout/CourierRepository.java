package com.sellsphere.client.checkout;

import com.sellsphere.common.entity.Courier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourierRepository extends JpaRepository<Courier, Integer> {

    Optional<Courier> findByName(String courierName);

}
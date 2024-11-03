package com.sellsphere.admin.transaction;

import com.sellsphere.common.entity.Charge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChargeRepository extends JpaRepository<Charge, Integer> {
}

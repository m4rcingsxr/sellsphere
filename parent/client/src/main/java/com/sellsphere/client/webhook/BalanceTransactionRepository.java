package com.sellsphere.client.webhook;

import com.sellsphere.common.entity.BalanceTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BalanceTransactionRepository extends JpaRepository<BalanceTransaction, Integer> {

}

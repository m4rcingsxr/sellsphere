package com.sellsphere.client.webhook;

import com.sellsphere.common.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRepository extends JpaRepository<Card, Integer> {

}
package com.sellsphere.admin.article;

import com.sellsphere.common.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Integer> {

    Optional<Promotion> findByName(String name);

}

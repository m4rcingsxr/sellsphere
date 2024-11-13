package com.sellsphere.client.product;

import com.sellsphere.common.entity.Category;
import com.sellsphere.common.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;


@Repository
public interface ProductRepository extends JpaRepository<Product, Integer>,
        JpaSpecificationExecutor<Product> {

    Optional<Product> findByAlias(String alias);

    @Query("SELECT MIN(p.price - (p.price * (p.discountPercent / 100))) FROM Product p")
    BigDecimal findMinPrice();

    @Query("SELECT MAX(p.price - (p.price * (p.discountPercent / 100))) FROM Product p")
    BigDecimal findMaxPrice();

    @Query("SELECT MAX(p.price - (p.price * (p.discountPercent / 100))) FROM Product p WHERE p.category = :category")
    BigDecimal findMinPrice(@Param("category") Category category);

    @Query("SELECT MIN(p.price - (p.price * (p.discountPercent / 100))) FROM Product p WHERE p.category = :category")
    BigDecimal findMaxPrice(@Param("category") Category category);
}

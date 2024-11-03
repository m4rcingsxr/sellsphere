package com.sellsphere.admin.order;

import com.sellsphere.common.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {

    @Query("SELECT NEW com.sellsphere.common.entity.OrderDetail(d.product" +
            ".category.name, d.quantity, d.productCost, d" +
            ".productPrice, d.subtotal, com.sellsphere.common.entity.DetailContext.CATEGORY, d.order) FROM OrderDetail d WHERE d.order" +
            ".orderTime BETWEEN :start AND :end")
    List<OrderDetail> findAllWithCategoryNameAndTimeBetween(
            @Param("start") LocalDateTime startDate,
            @Param("end") LocalDateTime endDate);

    @Query("SELECT NEW com.sellsphere.common.entity.OrderDetail(d.product.name," +
            " d.quantity, d.productCost, d" +
            ".productPrice, d.subtotal, com.sellsphere.common.entity.DetailContext.PRODUCT, d.order) FROM OrderDetail d WHERE d.order" +
            ".orderTime BETWEEN :start AND :end")
    List<OrderDetail> findAllWithProductNameAndTimeBetween(
            @Param("start") LocalDateTime startDate,
            @Param("end") LocalDateTime endDate);

}

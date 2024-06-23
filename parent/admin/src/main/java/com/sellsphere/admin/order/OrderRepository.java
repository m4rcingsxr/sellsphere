package com.sellsphere.admin.order;

import com.sellsphere.admin.page.SearchRepository;
import com.sellsphere.common.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends SearchRepository<Order, Integer> {

    @Override
    @Query("SELECT o FROM Order o WHERE LOWER(CONCAT(o.id, ' ', o.transaction.id)) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Order> findAll(@Param("keyword") String keyword, Pageable pageRequest);

}

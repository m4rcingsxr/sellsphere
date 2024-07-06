package com.sellsphere.client.order;

import com.sellsphere.common.entity.Customer;
import com.sellsphere.common.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    @Query("SELECT DISTINCT o FROM Order o " +
            "JOIN o.orderDetails od " +
            "JOIN od.product p " +
            "WHERE o.transaction.customer.id = ?1 " +
            "OR lower(p.name) LIKE concat('%', lower(?2), '%')")
    Page<Order> findAllByCustomerAndKeyword(Integer customerId, String keyword, Pageable pageRequest);

    Page<Order> findAllByTransactionCustomer(Customer customer, Pageable pageRequest);

    Optional<Order> findByIdAndTransactionCustomer(Integer orderId, Customer customer);

    List<Order> findAllByTransactionCustomer(Customer customer);

}

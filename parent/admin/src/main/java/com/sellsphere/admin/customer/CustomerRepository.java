package com.sellsphere.admin.customer;

import com.sellsphere.admin.page.SearchRepository;
import com.sellsphere.common.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends SearchRepository<Customer, Integer> {

    @Override
    @Query("SELECT c FROM Customer c WHERE LOWER(CONCAT(c.id, ' ', c.firstName, ' ', c" +
            ".lastName, ' ', c.email)) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Customer> findAll(String keyword, Pageable pageable);

    Optional<Customer> findByEmail(String email);

}

package com.sellsphere.admin.product;

import com.sellsphere.admin.page.SearchRepository;
import com.sellsphere.common.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends SearchRepository<Product, Integer> {

    @Override
    @Query("SELECT p FROM Product p WHERE LOWER(CONCAT(p.id, ' ', p.name, ' ', p" +
            ".shortDescription, ' ', p.fullDescription, ' ', " + "p.brand.name, ' ', p.category" + ".name)) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Product> findAll(@Param("keyword") String keyword, Pageable pageRequest);

}

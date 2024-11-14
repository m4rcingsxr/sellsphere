package com.sellsphere.admin.product;

import com.sellsphere.admin.page.SearchRepository;
import com.sellsphere.common.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends SearchRepository<Product, Integer> {

    @Override
    @Query("SELECT p FROM Product p WHERE LOWER(CONCAT(p.id, ' ', p.name, ' ', p" +
            ".shortDescription, ' ', p.fullDescription, ' ', " + "p.brand.name, ' ', p.category" + ".name)) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Product> findAll(@Param("keyword") String keyword, Pageable pageRequest);

    @Query("SELECT p FROM Product p WHERE LOWER(CONCAT(p.id, ' ', p.name, ' ', p" +
            ".shortDescription, ' ', p.fullDescription, ' ', " + "p.brand.name, ' ', p.category" + ".name)) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> findAll(@Param("keyword") String keyword, Sort sort);

    Optional<Product> findByName(String name);

    List<Product> findAllByBrandId(Integer brandId);

    List<Product> findAllByCategoryId(Integer id);

    List<Product> findAllByNameIn(List<String> names);

    long countAllByBrandId(Integer brandId);
}

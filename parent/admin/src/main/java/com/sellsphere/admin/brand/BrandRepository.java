package com.sellsphere.admin.brand;

import com.sellsphere.admin.page.SearchRepository;
import com.sellsphere.common.entity.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BrandRepository extends SearchRepository<Brand, Integer> {

    @Override
    @Query("SELECT b FROM Brand b WHERE LOWER(CONCAT(b.id, ' ', b.name, ' ', b" +
            ".logo)) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Brand> findAll(@Param("keyword") String keyword, Pageable pageable);

    Optional<Brand> findByName(String name);
}

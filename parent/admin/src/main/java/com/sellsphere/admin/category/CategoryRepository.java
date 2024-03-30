package com.sellsphere.admin.category;

import com.sellsphere.admin.page.SearchRepository;
import com.sellsphere.common.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends SearchRepository<Category, Integer> {


    @Override
    @Query("SELECT c FROM Category c WHERE LOWER(CONCAT(c.id, " + "' ', c.name, ' " + "', c" +
            ".alias)) LIKE LOWER(CONCAT('%', :keyword, " + "'%'))")
    Page<Category> findAll(@Param("keyword") String keyword, Pageable pageRequest);

    Page<Category> findAllByParentIsNull(Pageable pageable);

    List<Category> findAllByParentIsNull(Sort sort);
}
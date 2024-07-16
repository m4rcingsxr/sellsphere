package com.sellsphere.admin.question;

import com.sellsphere.admin.page.SearchRepository;
import com.sellsphere.common.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends SearchRepository<Question, Integer> {

    @Override
    @Query("SELECT q FROM Question q WHERE LOWER(CONCAT(q.id, ' ', q.product.name, ' ', q.customer.firstName, ' ', q.customer.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Question> findAll(@Param("keyword") String keyword, Pageable pageable);

}
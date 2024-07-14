package com.sellsphere.client.question;

import com.sellsphere.common.entity.Product;
import com.sellsphere.common.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer> {

    Page<Question> findAllByProductAndApprovalStatusIsTrue(Product product, Pageable pageable);

}

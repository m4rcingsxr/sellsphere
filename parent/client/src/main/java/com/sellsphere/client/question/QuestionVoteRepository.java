package com.sellsphere.client.question;

import com.sellsphere.common.entity.QuestionVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface QuestionVoteRepository extends JpaRepository<QuestionVote, Integer> {

    Optional<QuestionVote> findByQuestionIdAndCustomerId(Integer questionId, Integer id);

    @Query("SELECT v FROM QuestionVote v WHERE v.question.product.id = ?1 AND v.customer.id = ?2")
    List<QuestionVote> findAllByProductAndCustomer(Integer productId, Integer customerId);
}

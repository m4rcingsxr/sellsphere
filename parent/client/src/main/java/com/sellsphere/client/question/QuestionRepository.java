package com.sellsphere.client.question;

import com.sellsphere.common.entity.Product;
import com.sellsphere.common.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer> {

    Page<Question> findAllByProductAndApprovalStatusIsTrue(Product product, Pageable pageable);

    @Query("UPDATE Question q SET q.votes = (SELECT SUM(v.votes) FROM QuestionVote v WHERE v.question.id = ?1) WHERE q.id = ?1")
    @Modifying
    void updateVoteCount(Integer questionId);

    @Query("SELECT q.votes FROM Question q WHERE q.id = :questionId")
    Integer findVotesByQuestionId(@Param("questionId") Integer questionId);
}

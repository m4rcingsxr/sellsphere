package com.sellsphere.admin.question;

import com.sellsphere.common.entity.Question;
import com.sellsphere.common.entity.QuestionVote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionVoteRepository extends JpaRepository<QuestionVote, Integer> {

    void deleteAllByQuestion(Question question);

}

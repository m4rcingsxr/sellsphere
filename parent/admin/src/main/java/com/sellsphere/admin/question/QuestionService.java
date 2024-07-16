package com.sellsphere.admin.question;

import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.common.entity.Question;
import com.sellsphere.common.entity.QuestionNotFoundException;
import com.sellsphere.common.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Transactional
@RequiredArgsConstructor
@Service
public class QuestionService {

    private final QuestionRepository questionRepository;

    private static final int QUESTION_PER_PAGE = 10;

    public Question get(Integer id) throws QuestionNotFoundException {
        return questionRepository.findById(id).orElseThrow(QuestionNotFoundException::new);
    }

    public void listPage(int pageNum, PagingAndSortingHelper helper) {
        helper.listEntities(pageNum, QUESTION_PER_PAGE, questionRepository);
    }


    public Question save(Question question, User answerer) {
        question.setAnswerTime(LocalDate.now());
        question.setAnswerer(answerer.getFullName());

        return questionRepository.save(question);
    }
}

package com.sellsphere.admin.question;

import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.common.entity.Product;
import com.sellsphere.common.entity.Question;
import com.sellsphere.common.entity.QuestionNotFoundException;
import com.sellsphere.common.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Transactional
@RequiredArgsConstructor
@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final QuestionVoteRepository questionVoteRepository;

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

    public void delete(Integer id) throws QuestionNotFoundException {
        Question question = questionRepository.findById(id).orElseThrow(QuestionNotFoundException::new);
        questionVoteRepository.deleteAllByQuestion(question);

        questionRepository.delete(question);
    }

    public void changeApprovalStatus(Integer id, Boolean status) throws QuestionNotFoundException {
        Question question = questionRepository.findById(id).orElseThrow(QuestionNotFoundException::new);
        question.setApprovalStatus(status);

        Product product = question.getProduct();
        product.setQuestionCount(product.getQuestionCount() + 1);

        questionRepository.save(question);
    }

    public List<Question> listAll(String field, Sort.Direction direction) {
        return questionRepository.findAll(Sort.by(direction, field));
    }
}

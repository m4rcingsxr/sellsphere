package com.sellsphere.client.question;

import com.sellsphere.common.entity.Customer;
import com.sellsphere.common.entity.Product;
import com.sellsphere.common.entity.Question;
import com.sellsphere.common.entity.QuestionNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private static final int QUESTION_PER_PAGE = 10;

    private final QuestionRepository questionRepository;

    public void save(Question question) {
        question.setAskTime(LocalDate.now());
        question.setApprovalStatus(Boolean.FALSE);

        questionRepository.save(question);
    }

    public List<Question> find5MostPopularQuestions(Product product) {
        Sort sort = Sort.by("votes").descending();
        Pageable pageable = PageRequest.of(0, 5, sort);

        return questionRepository.findAllByProductAndApprovalStatusIsTrue(product, pageable).getContent();
    }

    public Page<Question> pageQuestions(Product product, Integer pageNum, String sortField) {
        Sort sort;

        switch (sortField != null ? sortField : "askTime") {
            case "leastPopular" -> sort = Sort.by("votes").ascending();
            case "mostPopular" -> sort = Sort.by("votes").descending();
            default -> sort = Sort.by("askTime");
        }

        Pageable pageable = PageRequest.of(pageNum, QUESTION_PER_PAGE, sort);
        return questionRepository.findAllByProductAndApprovalStatusIsTrue(product, pageable);
    }

    public List<Question> findQuestionsByCustomer(Customer customer) {
        return questionRepository.findAllByCustomer(customer);

    }

    public Question findById(Integer id) throws QuestionNotFoundException {
        return questionRepository.findById(id).orElseThrow(QuestionNotFoundException::new);
    }
}

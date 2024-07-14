package com.sellsphere.client.question;

import com.sellsphere.common.entity.Product;
import com.sellsphere.common.entity.Question;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {

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

}

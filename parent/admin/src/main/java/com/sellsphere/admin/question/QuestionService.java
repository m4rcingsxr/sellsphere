package com.sellsphere.admin.question;

import com.sellsphere.admin.page.PagingAndSortingHelper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Transactional
@RequiredArgsConstructor
@Service
public class QuestionService {

    private final QuestionRepository questionRepository;

    private static final int QUESTION_PER_PAGE = 10;

    public void listPage(int pageNum, PagingAndSortingHelper helper) {
        helper.listEntities(pageNum, QUESTION_PER_PAGE, questionRepository);
    }


}

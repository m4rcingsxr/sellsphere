package com.sellsphere.admin.question;

import com.sellsphere.common.entity.Question;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.jdbc.Sql;
import util.PagingTestHelper;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Sql(scripts = {
        "classpath:sql/categories.sql",
        "classpath:sql/brands.sql",
        "classpath:sql/brands_categories.sql",
        "classpath:sql/customers.sql",
        "classpath:sql/products.sql",
        "classpath:sql/questions.sql"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class QuestionRepositoryTest {

    @Autowired
    private QuestionRepository questionRepository;

    @Test
    void givenSortByAskTimeAsc_whenFindingAll_thenReturnSortedQuestionsInAscOrder() {
        // When
        Page<Question> result = questionRepository.findAll(PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "askTime")));

        // Then
        assertFalse(result.isEmpty(), "Questions should not be empty.");
        assertTrue(result.getContent().get(0).getAskTime()
                           .isBefore(result.getContent().get(1).getAskTime()),
                   "Questions should be sorted by ask time in ascending order.");
    }

    @Test
    void givenSortByAskTimeDesc_whenFindingAll_thenReturnSortedQuestionsInDescOrder() {
        // When
        Page<Question> result = questionRepository.findAll(PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "askTime")));

        // Then
        assertFalse(result.isEmpty(), "Questions should not be empty.");
        assertTrue(result.getContent().get(0).getAskTime()
                           .isAfter(result.getContent().get(1).getAskTime()),
                   "Questions should be sorted by ask time in descending order.");
    }

    @Test
    void givenKeywordAndPaging_whenSearching_thenReturnPagedResults() {
        // Given
        String keyword = "Canon";
        PageRequest pageable = PageRequest.of(0, 1, Sort.by("askTime"));

        // When
        Page<Question> result = questionRepository.findAll(keyword, pageable);

        // Then
        PagingTestHelper.assertPagingResults(result, 1, 1, 1, "askTime", true);
    }

    @Test
    void givenPageOutOfBounds_whenPaging_thenReturnEmptyPage() {
        // Given
        PageRequest pageable = PageRequest.of(5, 5);

        // When
        Page<Question> result = questionRepository.findAll(pageable);

        // Then
        assertTrue(result.isEmpty(), "Result should be an empty page.");
        assertEquals(0, result.getContent().size(), "Page content size should be 0.");
    }

    @Test
    void givenEmptyKeyword_whenSearching_thenReturnAllQuestions() {
        // Given
        String keyword = "";
        PageRequest pageable = PageRequest.of(0, 3);

        // When
        Page<Question> result = questionRepository.findAll(keyword, pageable);

        // Then
        assertFalse(result.isEmpty(), "All questions should be returned when the keyword is empty.");
    }
}

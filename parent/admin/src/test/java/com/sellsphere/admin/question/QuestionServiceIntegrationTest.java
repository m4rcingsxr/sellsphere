package com.sellsphere.admin.question;

import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.common.entity.Question;
import com.sellsphere.common.entity.QuestionNotFoundException;
import com.sellsphere.common.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Sql(scripts = {
        "classpath:sql/categories.sql",
        "classpath:sql/brands.sql",
        "classpath:sql/brands_categories.sql",
        "classpath:sql/customers.sql",
        "classpath:sql/products.sql",
        "classpath:sql/questions.sql"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class QuestionServiceIntegrationTest {

    @Autowired
    private QuestionService questionService;

    @Test
    void givenValidQuestionId_whenGet_thenReturnQuestion() throws QuestionNotFoundException {
        // Given
        Integer questionId = 1;

        // When
        Question question = questionService.get(questionId);

        // Then
        assertNotNull(question, "Question should be found by ID.");
        assertEquals(questionId, question.getId(), "Returned question ID should match the expected ID.");
    }

    @Test
    void givenInvalidQuestionId_whenGet_thenThrowQuestionNotFoundException() {
        // Given
        Integer questionId = 999;

        // When/Then
        assertThrows(QuestionNotFoundException.class, () -> questionService.get(questionId),
                     "Expected QuestionNotFoundException for invalid question ID.");
    }

    @Test
    void givenPageRequest_whenListPage_thenReturnPagedQuestions() {
        // Given
        int pageNum = 0;

        // When
        PagingAndSortingHelper helper = new PagingAndSortingHelper(new ModelAndViewContainer(), "questionList", "askTime", Sort.Direction.ASC, null);
        questionService.listPage(pageNum, helper);

        // Then
        assertEquals(5, helper.getContent().size());
    }

    @Test
    void givenQuestionAndAnswerer_whenSave_thenQuestionIsUpdatedWithAnswer() throws QuestionNotFoundException {
        // Given
        Integer questionId = 1;
        Question question = questionService.get(questionId);
        User answerer = new User();
        answerer.setFirstName("Jane");
        answerer.setLastName("Smith");

        question.setAnswerContent("This is an answer to the question.");

        // When
        Question savedQuestion = questionService.save(question, answerer);

        // Then
        assertEquals("Jane Smith", savedQuestion.getAnswerer(), "Answerer name should be set correctly.");
        assertEquals(LocalDate.now(), savedQuestion.getAnswerTime(), "Answer time should be set to today's date.");
        assertEquals("This is an answer to the question.", savedQuestion.getAnswerContent(),
                     "Answer content should match the saved content.");
    }

    @Test
    void givenExistingQuestionId_whenDelete_thenQuestionAndVotesAreDeleted() throws QuestionNotFoundException {
        // Given
        Integer questionId = 1;

        // When
        questionService.delete(questionId);

        // Then
        assertThrows(QuestionNotFoundException.class, () -> questionService.get(questionId),
                     "Deleted question should not be retrievable.");
    }

    @Test
    void givenInvalidQuestionId_whenDelete_thenThrowQuestionNotFoundException() {
        // Given
        Integer questionId = 999;

        // When/Then
        assertThrows(QuestionNotFoundException.class, () -> questionService.delete(questionId),
                     "Expected QuestionNotFoundException for invalid question ID.");
    }

    @Test
    void givenExistingQuestionIdAndStatus_whenChangeApprovalStatus_thenStatusIsUpdatedAndCountIncremented() throws QuestionNotFoundException {
        // Given
        Integer questionId = 1;
        Boolean newStatus = true;

        Question question = questionService.get(questionId);
        int initialQuestionCount = question.getProduct().getQuestionCount();

        // When
        questionService.changeApprovalStatus(questionId, newStatus);

        // Then
        assertTrue(question.getApprovalStatus(), "Approval status should be updated to true.");
        assertEquals(initialQuestionCount + 1, question.getProduct().getQuestionCount(),
                     "Product question count should be incremented.");
    }

    @Test
    void givenInvalidQuestionId_whenChangeApprovalStatus_thenThrowQuestionNotFoundException() {
        // Given
        Integer questionId = 999;
        Boolean newStatus = true;

        // When/Then
        assertThrows(QuestionNotFoundException.class, () -> questionService.changeApprovalStatus(questionId, newStatus),
                     "Expected QuestionNotFoundException for invalid question ID.");
    }
}

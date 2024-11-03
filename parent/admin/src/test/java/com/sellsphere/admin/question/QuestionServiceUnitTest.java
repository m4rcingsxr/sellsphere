package com.sellsphere.admin.question;

import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.common.entity.Product;
import com.sellsphere.common.entity.Question;
import com.sellsphere.common.entity.QuestionNotFoundException;
import com.sellsphere.common.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class QuestionServiceUnitTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private QuestionVoteRepository questionVoteRepository;

    @Mock
    private PagingAndSortingHelper pagingAndSortingHelper;

    @InjectMocks
    private QuestionService questionService;

    @Test
    void givenExistingQuestionId_whenGet_thenReturnQuestion() throws QuestionNotFoundException {
        // Given
        Integer questionId = 1;
        Question question = new Question();
        question.setId(questionId);
        given(questionRepository.findById(questionId)).willReturn(Optional.of(question));

        // When
        Question foundQuestion = questionService.get(questionId);

        // Then
        assertEquals(questionId, foundQuestion.getId(), "Returned question ID should match the expected ID.");
        then(questionRepository).should().findById(questionId);
    }

    @Test
    void givenNonExistingQuestionId_whenGet_thenThrowQuestionNotFoundException() {
        // Given
        Integer questionId = 999;
        given(questionRepository.findById(questionId)).willReturn(Optional.empty());

        // When/Then
        assertThrows(QuestionNotFoundException.class, () -> questionService.get(questionId));
        then(questionRepository).should().findById(questionId);
    }

    @Test
    void givenPagingRequest_whenListPage_thenListEntitiesCalled() {
        // Given
        int pageNum = 0;

        // When
        questionService.listPage(pageNum, pagingAndSortingHelper);

        // Then
        then(pagingAndSortingHelper).should().listEntities(eq(pageNum), eq(10), eq(questionRepository));
    }

    @Test
    void givenValidQuestionAndAnswerer_whenSave_thenQuestionIsSavedWithAnswer() {
        // Given
        User answerer = new User();
        answerer.setFirstName("John");
        answerer.setLastName("Doe");

        Question question = new Question();
        question.setId(1);
        question.setAnswerContent("Sample Answer");

        given(questionRepository.save(any(Question.class))).willReturn(question);

        // When
        Question savedQuestion = questionService.save(question, answerer);

        // Then
        assertEquals("John Doe", savedQuestion.getAnswerer(), "Answerer name should be correctly set.");
        assertEquals(LocalDate.now(), savedQuestion.getAnswerTime(), "Answer time should be set to current date.");
        then(questionRepository).should().save(question);
    }

    @Test
    void givenExistingQuestionId_whenDelete_thenQuestionAndVotesDeleted() throws QuestionNotFoundException {
        // Given
        Integer questionId = 1;
        Question question = new Question();
        question.setId(questionId);

        given(questionRepository.findById(questionId)).willReturn(Optional.of(question));

        // When
        questionService.delete(questionId);

        // Then
        then(questionVoteRepository).should().deleteAllByQuestion(question);
        then(questionRepository).should().delete(question);
    }

    @Test
    void givenNonExistingQuestionId_whenDelete_thenThrowQuestionNotFoundException() {
        // Given
        Integer questionId = 999;
        given(questionRepository.findById(questionId)).willReturn(Optional.empty());

        // When/Then
        assertThrows(QuestionNotFoundException.class, () -> questionService.delete(questionId));
        then(questionRepository).should().findById(questionId);
    }

    @Test
    void givenExistingQuestionId_whenChangeApprovalStatus_thenUpdateStatusAndIncrementProductQuestionCount() throws QuestionNotFoundException {
        // Given
        Integer questionId = 1;
        Boolean newStatus = true;

        Question question = new Question();
        question.setId(questionId);
        question.setApprovalStatus(false);

        Product product = new Product();
        product.setQuestionCount(0);
        question.setProduct(product);

        given(questionRepository.findById(questionId)).willReturn(Optional.of(question));

        // When
        questionService.changeApprovalStatus(questionId, newStatus);

        // Then
        assertTrue(question.getApprovalStatus(), "Approval status should be updated to the new value.");
        assertEquals(1, product.getQuestionCount(), "Product question count should be incremented.");
        then(questionRepository).should().save(question);
    }

    @Test
    void givenNonExistingQuestionId_whenChangeApprovalStatus_thenThrowQuestionNotFoundException() {
        // Given
        Integer questionId = 999;
        Boolean newStatus = true;

        given(questionRepository.findById(questionId)).willReturn(Optional.empty());

        // When/Then
        assertThrows(QuestionNotFoundException.class, () -> questionService.changeApprovalStatus(questionId, newStatus));
        then(questionRepository).should().findById(questionId);
    }
}

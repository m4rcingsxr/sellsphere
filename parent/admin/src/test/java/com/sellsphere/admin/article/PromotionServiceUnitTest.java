package com.sellsphere.admin.article;

import com.sellsphere.common.entity.Article;
import com.sellsphere.common.entity.Promotion;
import com.sellsphere.common.entity.PromotionNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class PromotionServiceUnitTest {

    @Mock
    private PromotionRepository promotionRepository;

    @InjectMocks
    private PromotionService promotionService;

    private Promotion promotionMock;
    private Article articleMock;

    @BeforeEach
    void setup() {
        promotionMock = new Promotion();
        promotionMock.setId(1);
        promotionMock.setName("Winter Sale");

        articleMock = new Article();
        articleMock.setPromotion(promotionMock);
    }

    @Test
    void givenPromotionExists_whenGetByName_thenReturnsPromotion() throws PromotionNotFoundException {
        // Given
        String promotionName = "Winter Sale";
        given(promotionRepository.findByName(promotionName)).willReturn(Optional.of(promotionMock));

        // When
        Promotion result = promotionService.getByName(promotionName);

        // Then
        assertNotNull(result);
        assertEquals(promotionMock, result);
        then(promotionRepository).should().findByName(promotionName);
    }

    @Test
    void givenPromotionDoesNotExist_whenGetByName_thenThrowsPromotionNotFoundException() {
        // Given
        String promotionName = "Summer Sale";
        given(promotionRepository.findByName(promotionName)).willReturn(Optional.empty());

        // When / Then
        assertThrows(PromotionNotFoundException.class, () -> promotionService.getByName(promotionName));
        then(promotionRepository).should().findByName(promotionName);
    }

    @Test
    void givenPromotion_whenSave_thenSavesAndReturnsPromotion() {
        // Given
        given(promotionRepository.save(promotionMock)).willReturn(promotionMock);

        // When
        Promotion result = promotionService.save(promotionMock);

        // Then
        assertNotNull(result);
        assertEquals(promotionMock, result);
        then(promotionRepository).should().save(promotionMock);
    }

    @Test
    void givenArticleWithPromotion_whenGetByArticle_thenReturnsPromotion() throws PromotionNotFoundException {
        // Given
        given(promotionRepository.findByName(articleMock.getPromotion().getName())).willReturn(Optional.of(promotionMock));

        // When
        Promotion result = promotionService.getByArticle(articleMock);

        // Then
        assertNotNull(result);
        assertEquals(promotionMock, result);
        then(promotionRepository).should().findByName(articleMock.getPromotion().getName());
    }

    @Test
    void givenPromotionDoesNotExistForArticle_whenGetByArticle_thenThrowsPromotionNotFoundException() {
        // Given
        given(promotionRepository.findByName(promotionMock.getName())).willReturn(Optional.empty());

        // When / Then
        assertThrows(PromotionNotFoundException.class, () -> promotionService.getByArticle(articleMock));
        then(promotionRepository).should().findByName(articleMock.getPromotion().getName());
    }

    @Test
    void givenPromotionsExist_whenListAll_thenReturnsAllPromotionsSortedByName() {
        // Given
        List<Promotion> promotions = List.of(promotionMock);
        given(promotionRepository.findAll(any(Sort.class))).willReturn(promotions);

        // When
        List<Promotion> result = promotionService.listAll();

        // Then
        assertNotNull(result);
        assertEquals(promotions, result);
        then(promotionRepository).should().findAll(any(Sort.class));
    }

    @Test
    void givenNoPromotionsExist_whenListAll_thenReturnsEmptyList() {
        // Given
        given(promotionRepository.findAll(any(Sort.class))).willReturn(List.of());

        // When
        List<Promotion> result = promotionService.listAll();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        then(promotionRepository).should().findAll(any(Sort.class));
    }

    @Test
    void givenPromotionExists_whenGetById_thenReturnsPromotion() throws PromotionNotFoundException {
        // Given
        Integer promotionId = 1;
        given(promotionRepository.findById(promotionId)).willReturn(Optional.of(promotionMock));

        // When
        Promotion result = promotionService.getById(promotionId);

        // Then
        assertNotNull(result);
        assertEquals(promotionMock, result);
        then(promotionRepository).should().findById(promotionId);
    }

    @Test
    void givenPromotionDoesNotExist_whenGetById_thenThrowsPromotionNotFoundException() {
        // Given
        Integer promotionId = 2;
        given(promotionRepository.findById(promotionId)).willReturn(Optional.empty());

        // When / Then
        assertThrows(PromotionNotFoundException.class, () -> promotionService.getById(promotionId));
        then(promotionRepository).should().findById(promotionId);
    }

    @Test
    void givenPromotion_whenDelete_thenDeletesPromotion() {
        // When
        promotionService.delete(promotionMock);

        // Then
        then(promotionRepository).should().delete(promotionMock);
    }
}

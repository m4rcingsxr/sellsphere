package com.sellsphere.admin.article;

import com.sellsphere.common.entity.*;
import com.sellsphere.common.entity.payload.ArticleMetadata;
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
class FooterSectionServiceUnitTest {

    @Mock
    private FooterSectionRepository footerSectionRepository;

    @Mock
    private FooterItemRepository footerItemRepository;

    @InjectMocks
    private FooterSectionService footerSectionService;

    private FooterSection footerSectionMock1;
    private FooterSection footerSectionMock2;

    private ArticleMetadata articleMetadataMock;

    private Article articleMock1;
    private Article articleMock2;

    @BeforeEach
    void setup() {
        FooterItem footerItem = new FooterItem();
        articleMock2 = new Article();
        articleMock2.setId(2);
        footerItem.setArticle(articleMock2);
        footerItem.setItemNumber(2);

        footerSectionMock1 = new FooterSection();
        footerSectionMock1.addFooterItem(footerItem);

        footerSectionMock2 = new FooterSection();

        articleMock1 = new Article();
        articleMock1.setId(1);

        articleMetadataMock = ArticleMetadata.builder()
                .article(articleMock1) // article passed by user
                .sectionHeader(new String[]{"1", "2", "3"})
                .sectionNumber(1)
                .itemNumber(1)
                .promotionName("Best tv 4k")
                .user(new User())
                .selectedProducts(List.of(1,2,3))
                .build();
    }

    @Test
    void givenFooterSectionsExist_whenFindAll_thenReturnsAllFooterSectionsSortedBySectionNumber() {

        // given
        given(footerSectionRepository.findAll(any(Sort.class))).willReturn(List.of(footerSectionMock1, footerSectionMock2));

        // when
        List<FooterSection> footerSectionList = footerSectionService.findAll();

        // then
        assertEquals(2, footerSectionList.size());

        then(footerSectionRepository).should().findAll(any(Sort.class));
    }

    @Test
    void givenArticleAndMetadata_whenSaveNewFooterSection_thenCreatesNewFooterSection() {
        // Given
        given(footerSectionRepository.findBySectionNumber(articleMetadataMock.getSectionNumber())).willReturn(Optional.empty());
        given(footerItemRepository.findByArticle(articleMetadataMock.getArticle())).willReturn(Optional.empty());

        // Use an Answer to capture and return the actual FooterSection created in the service method
        given(footerSectionRepository.save(any(FooterSection.class))).willAnswer(invocation -> invocation.getArgument(0));

        // When
        FooterSection footerSection = footerSectionService.save(articleMetadataMock);

        // Then
        assertNotNull(footerSection);
        assertEquals(articleMetadataMock.getSectionNumber(), footerSection.getSectionNumber());
        assertEquals(articleMetadataMock.getSectionHeader()[articleMetadataMock.getSectionNumber() - 1], footerSection.getSectionHeader());
        assertEquals(1, footerSection.getFooterItems().size());
        assertEquals(articleMetadataMock.getItemNumber(), footerSection.getFooterItems().get(0).getItemNumber());
        assertEquals(articleMock1, footerSection.getFooterItems().get(0).getArticle());
    }

    @Test
    void givenNewArticleAndMetadata_whenSaveExistingFooterSectionAndFooterItem_thenUpdateExistingFooterSectionAndSetPreviousArticleToTypeFree() {
        // Given
        // same item number
        articleMetadataMock.setItemNumber(2);
        given(footerSectionRepository.findBySectionNumber(articleMetadataMock.getSectionNumber())).willReturn(Optional.of(footerSectionMock1));

        // Use an Answer to capture and return the actual FooterSection created in the service method
        given(footerSectionRepository.save(any(FooterSection.class))).willReturn(footerSectionMock1);

        // When
        FooterSection footerSection = footerSectionService.save(articleMetadataMock);

        // Then
        assertNotNull(footerSection);
        assertEquals(ArticleType.FREE, articleMock2.getArticleType());
        assertEquals(footerSection.getFooterItems().get(0).getArticle(), articleMock1);
    }

    @Test
    void givenNewArticleAndMetadata_whenSaveExistingFooter_thenUpdateExistingFooterSectionWithNewFooterItem() {
        // Given
        footerSectionMock1.setSectionHeader(articleMetadataMock.getSectionHeader()[articleMetadataMock.getSectionNumber() - 1]);
        footerSectionMock1.setSectionNumber(articleMetadataMock.getSectionNumber());

        // Mock the existing footer section retrieval by section number
        given(footerSectionRepository.findBySectionNumber(articleMetadataMock.getSectionNumber())).willReturn(Optional.of(footerSectionMock1));

        // Return the same footerSectionMock1 when saved, capturing any updates to it
        given(footerSectionRepository.save(any(FooterSection.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(footerItemRepository.findByArticle(articleMetadataMock.getArticle())).willReturn(Optional.empty());

        // When
        FooterSection result = footerSectionService.save(articleMetadataMock);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getSectionNumber());
        assertEquals("1", result.getSectionHeader());
        assertEquals(2, result.getFooterItems().size()); // Initially had 1, should now have 2 after adding the new item

        // Validate the new footer item was added correctly
        FooterItem addedItem = result.getFooterItems().stream()
                .filter(item -> item.getItemNumber().equals(articleMetadataMock.getItemNumber()))
                .findFirst()
                .orElse(null);

        assertNotNull(addedItem);
        assertEquals(articleMetadataMock.getItemNumber(), addedItem.getItemNumber());
        assertEquals(articleMock1, addedItem.getArticle());
    }

    @Test
    void givenSectionNumberExists_whenGet_thenReturnsFooterSection() {
        // Given
        Integer sectionNumber = 1;
        given(footerSectionRepository.findBySectionNumber(sectionNumber)).willReturn(Optional.of(footerSectionMock1));

        // When
        Optional<FooterSection> result = footerSectionService.get(sectionNumber);

        // Then
        assertTrue(result.isPresent());
        assertEquals(footerSectionMock1, result.get());
        then(footerSectionRepository).should().findBySectionNumber(sectionNumber);
    }

    @Test
    void givenSectionNumberDoesNotExist_whenGet_thenReturnsEmptyOptional() {
        // Given
        Integer sectionNumber = 999;
        given(footerSectionRepository.findBySectionNumber(sectionNumber)).willReturn(Optional.empty());

        // When
        Optional<FooterSection> result = footerSectionService.get(sectionNumber);

        // Then
        assertFalse(result.isPresent());
        then(footerSectionRepository).should().findBySectionNumber(sectionNumber);
    }

    @Test
    void givenFooterSection_whenSave_thenSavesAndReturnsFooterSection() {
        // Given
        given(footerSectionRepository.save(footerSectionMock1)).willReturn(footerSectionMock1);

        // When
        FooterSection result = footerSectionService.save(footerSectionMock1);

        // Then
        assertNotNull(result);
        assertEquals(footerSectionMock1, result);
        then(footerSectionRepository).should().save(footerSectionMock1);
    }

    @Test
    void givenArticle_whenGetFooterItemByArticle_thenReturnsFooterItem() throws ArticleNotFoundException {
        // Given
        FooterItem footerItem = new FooterItem();
        given(footerItemRepository.findByArticle(articleMock1)).willReturn(Optional.of(footerItem));

        // When
        FooterItem result = footerSectionService.getFooterItemByArticle(articleMock1);

        // Then
        assertEquals(footerItem, result);
        then(footerItemRepository).should().findByArticle(articleMock1);
    }

    @Test
    void givenNonExistentArticle_whenGetFooterItemByArticle_thenThrowsArticleNotFoundException() {
        // Given
        given(footerItemRepository.findByArticle(articleMock1)).willReturn(Optional.empty());

        // When/Then
        assertThrows(ArticleNotFoundException.class, () -> footerSectionService.getFooterItemByArticle(articleMock1));
        then(footerItemRepository).should().findByArticle(articleMock1);
    }



}
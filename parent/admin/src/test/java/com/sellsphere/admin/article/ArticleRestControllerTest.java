package com.sellsphere.admin.article;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sellsphere.common.entity.Article;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ArticleRestControllerTest {

    @MockBean
    private ArticleService articleService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void givenExistingTitles_whenFindArticlesByNames_thenReturnArticleDTOs() throws Exception {
        // Arrange
        Article article1 = new Article();
        article1.setTitle("Title1");
        Article article2 = new Article();
        article2.setTitle("Title2");

        List<Article> articles = List.of(article1, article2);
        given(articleService.findAllByTitles(anyList())).willReturn(articles);

        // Act & Assert
        mockMvc.perform(post("/articles/titles")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(List.of("Title1", "Title2"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Title1"))
                .andExpect(jsonPath("$[1].title").value("Title2"));

        then(articleService).should().findAllByTitles(List.of("Title1", "Title2"));
    }

    @Test
    void givenNonExistentTitles_whenFindArticlesByNames_thenReturnNotFound() throws Exception {
        // Arrange
        given(articleService.findAllByTitles(anyList())).willReturn(List.of());

        // Act & Assert
        mockMvc.perform(post("/articles/titles")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(List.of("UnknownTitle"))))
                .andExpect(status().isNotFound());

        then(articleService).should().findAllByTitles(List.of("UnknownTitle"));
    }

    @Test
    void givenUniqueArticleTitle_whenCheckArticleTitleUniqueness_thenReturnTrue() throws Exception {
        // Arrange
        given(articleService.isArticleNameUnique(anyInt(), eq("UniqueTitle"))).willReturn(true);

        // Act & Assert
        mockMvc.perform(post("/articles/check_uniqueness")
                                .param("id", "1")
                                .param("title", "UniqueTitle")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        then(articleService).should().isArticleNameUnique(1, "UniqueTitle");
    }

    @Test
    void givenNonUniqueArticleTitle_whenCheckArticleTitleUniqueness_thenReturnFalse() throws Exception {
        // Arrange
        given(articleService.isArticleNameUnique(anyInt(), eq("ExistingTitle"))).willReturn(false);

        // Act & Assert
        mockMvc.perform(post("/articles/check_uniqueness")
                                .param("id", "1")
                                .param("title", "ExistingTitle")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));

        then(articleService).should().isArticleNameUnique(1, "ExistingTitle");
    }
}

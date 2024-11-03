package com.sellsphere.admin.article;

import com.sellsphere.common.entity.Article;
import com.sellsphere.common.entity.payload.ArticleDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class ArticleRestController {

    private final ArticleService articleService;

    /**
     * Endpoint to find articles by their titles.
     * This method takes a list of titles and returns the corresponding articles, if found.
     *
     * @param titles A list of article titles to search for.
     * @return A ResponseEntity containing a list of ArticleDTO objects if found,
     *         or a 404 status if no articles are found.
     */
    @PostMapping("/articles/titles")
    public ResponseEntity<List<ArticleDTO>> findArticleByNames(@RequestBody List<String> titles) {

        // Fetch the articles based on the provided titles
        List<Article> articles = articleService.findAllByTitles(titles);

        if (articles.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        List<ArticleDTO> articleDTOs = articles.stream().map(ArticleDTO::new).toList();
        return ResponseEntity.ok(articleDTOs);
    }

    @PostMapping("/articles/check_uniqueness")
    public boolean isProductUnique(
            @RequestParam(value = "id", required = false) Integer id,
            @RequestParam("title") String title
    ) {
        return articleService.isArticleNameUnique(id, title);
    }

}

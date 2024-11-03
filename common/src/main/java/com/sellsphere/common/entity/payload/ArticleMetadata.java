package com.sellsphere.common.entity.payload;

import com.sellsphere.common.entity.Article;
import com.sellsphere.common.entity.User;
import com.sellsphere.common.entity.constraints.ValidArticleMetadata;
import jakarta.validation.Valid;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidArticleMetadata
public class ArticleMetadata {

    @Valid
    private Article article;

    private User user;

    private Integer itemNumber;

    private Integer sectionNumber;

    private String[] sectionHeader;

    private String promotionName;

    private List<Integer> selectedProducts;
}
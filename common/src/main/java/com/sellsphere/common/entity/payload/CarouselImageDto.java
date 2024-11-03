package com.sellsphere.common.entity.payload;

import com.sellsphere.common.entity.CarouselImage;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link com.sellsphere.common.entity.CarouselImage}
 */
@Value
public class CarouselImageDto implements Serializable {
    Integer id;
    String name;
    String imagePath;
    ArticleDTO article;

    public CarouselImageDto(CarouselImage other) {
        this.id = other.getId();
        this.name = other.getName();
        this.imagePath = other.getImagePath();
        this.article = new ArticleDTO(other.getArticle());
    }
}
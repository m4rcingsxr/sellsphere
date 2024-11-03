package com.sellsphere.admin.mainpage;

import com.sellsphere.common.entity.Article;
import com.sellsphere.common.entity.CarouselImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarouselImageRepository extends JpaRepository<CarouselImage, Integer> {

    Optional<CarouselImage> findByArticle(Article article);
}

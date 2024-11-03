package com.sellsphere.admin.mainpage;

import com.sellsphere.common.entity.Carousel;
import com.sellsphere.common.entity.CarouselItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarouselItemRepository extends JpaRepository<CarouselItem, Integer> {


    List<CarouselItem> findAllByCarousel(Carousel carousel);

}

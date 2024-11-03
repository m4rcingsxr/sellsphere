package com.sellsphere.client.mainpage;

import com.sellsphere.common.entity.Carousel;
import com.sellsphere.common.entity.CarouselType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarouselRepository extends JpaRepository<Carousel, Integer> {

    List<Carousel> findAllByTypeInOrderByCarouselOrder(List<CarouselType> carouselTypes);

    List<Carousel> findAllByTypeOrderByCarouselOrder(CarouselType type);
}

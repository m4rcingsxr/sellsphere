package com.sellsphere.admin.mainpage;

import com.sellsphere.common.entity.Carousel;
import com.sellsphere.common.entity.CarouselType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarouselRepository extends JpaRepository<Carousel, Integer> {

    Optional<Carousel> findFirstByType(CarouselType type);
}

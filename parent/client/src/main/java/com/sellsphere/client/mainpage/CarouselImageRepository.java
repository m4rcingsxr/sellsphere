package com.sellsphere.client.mainpage;

import com.sellsphere.common.entity.CarouselImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarouselImageRepository extends JpaRepository<CarouselImage, Integer> {
}

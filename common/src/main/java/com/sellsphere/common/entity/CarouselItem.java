package com.sellsphere.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "carousel_items")
public class CarouselItem extends IdentifiedEntity{

    @Column(name = "entity_id", nullable = false)
    private Integer entityId; // product/article/carouselImage

    @ManyToOne
    @JoinColumn(name = "carousel_id", nullable = false)
    private Carousel carousel;

    @Column(name = "carousel_item_order", nullable = false)
    private Integer order;

    public CarouselItem(Integer entityId, Integer order) {
        this.entityId = entityId;
        this.order = order;
    }

}

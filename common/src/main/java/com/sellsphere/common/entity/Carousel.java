package com.sellsphere.common.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "carousels")
public class Carousel extends IdentifiedEntity {

    @NotNull(message = "Carousel type is required")
    @Column(name = "carousel_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private CarouselType type;

    @OneToMany(mappedBy = "carousel", cascade = {CascadeType.ALL},
            orphanRemoval = true)
    @OrderBy("order")
    private List<CarouselItem> carouselItems = new ArrayList<>();

    @NotNull(message = "Header is required")
    @Column(name = "header", nullable = false)
    private String header;

    @Column(name = "carousel_order", nullable = false, unique = true)
    private Integer carouselOrder;

    @OneToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name = "promotion_id", nullable = true)
    private Promotion promotion;

    public void addCarouselItem(CarouselItem item) {
        carouselItems.add(item);
        item.setCarousel(this);
    }

    public void removeCarouselItem(CarouselItem item) {
        carouselItems.remove(item);
        item.setCarousel(null);
    }

    public void setBidirectionalRelationship() {
        carouselItems.forEach(item -> item.setCarousel(this));
    }
}

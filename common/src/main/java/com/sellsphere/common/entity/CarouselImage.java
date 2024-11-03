package com.sellsphere.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// promotion type is intended for carousels on main page, free intended to navigation articles dropdown
// footer and navigation are informative articles - like contact, terms of use etc...

// if promotion type is selected in form then - display promotion form which will add offer to product details of selected products
// on save if it's type promotion then create the filters to construct the href to take user to the products that match the offer
@Getter
@Setter
@Entity
@Table(name = "carousel_images")
@NoArgsConstructor
public class CarouselImage extends IdentifiedEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @OneToOne(optional = false, cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH })
    @JoinColumn(name = "article_id", referencedColumnName = "id", nullable = false)
    private Article article;

    public CarouselImage(String name) {
        this.name = name;
    }

    @Transient
    public String getImagePath() {
        return Constants.S3_BASE_URI + "/main/carousel/" + name;
    }

}
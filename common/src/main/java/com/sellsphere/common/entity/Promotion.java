package com.sellsphere.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "promotions")
public class Promotion extends IdentifiedEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToMany
    @JoinTable(
            name = "promotion_product",
            joinColumns = @JoinColumn(name = "promotion_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private List<Product> products;

    @OneToMany(mappedBy = "promotion")
    private List<Article> articles;

    public void addArticle(Article savedArticle) {
        if (articles == null) {
            articles = new ArrayList<>();
        }
        savedArticle.setPromotion(this);
        articles.add(savedArticle);
    }
}

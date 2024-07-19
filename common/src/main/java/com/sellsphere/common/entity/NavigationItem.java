package com.sellsphere.common.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "navigation_items")
public class NavigationItem  extends IdentifiedEntity {

    @Min(1)
    @Max(5)
    @Column(name = "item_number", nullable = false)
    private Integer itemNumber;

    @OneToOne
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

}

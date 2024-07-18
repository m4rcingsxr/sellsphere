package com.sellsphere.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table
public class Promotion extends IdentifiedEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @OneToOne
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

}

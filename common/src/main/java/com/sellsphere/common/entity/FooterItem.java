package com.sellsphere.common.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "footer_items")
public class FooterItem extends IdentifiedEntity {

    @Column(name = "item_number", nullable = false)
    private Integer itemNumber;

    @OneToOne
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "footer_section_id", nullable = false)
    private FooterSection footerSection;

}
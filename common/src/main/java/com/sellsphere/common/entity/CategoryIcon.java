package com.sellsphere.common.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "category_icons")
public class CategoryIcon extends IdentifiedEntity {

    @Size(max = 128, message = "Category icon must be between 1 and 128 characters")
    @Column(name = "icon_path", length = 128, nullable = false)
    private String iconPath;

    @OneToOne
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private Category category;

    public CategoryIcon(Integer id, String iconPath) {
        super(id);
        this.iconPath = iconPath;
    }
}
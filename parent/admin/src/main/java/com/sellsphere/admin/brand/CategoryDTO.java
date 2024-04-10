package com.sellsphere.admin.brand;

import com.sellsphere.common.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {

    private Integer id;
    private String name;

    public CategoryDTO(Category category) {
        this(category.getId(), category.getName());
    }

}
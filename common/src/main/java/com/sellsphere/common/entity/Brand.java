package com.sellsphere.common.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "brands")
public class Brand extends IdentifiedEntity{

    @NotNull(message = "Brand name is required")
    @Size(min = 1, max = 45, message = "brand name must be between 1 and 45 characters")
    @Column(name = "name", length = 45, nullable = false, unique = true)
    private String name;

    @Size(min = 1, max = 45, message = "logo name must be between 1 and 45 characters")
    @Column(name = "logo", length = 45, nullable = false)
    private String logo;

    @ManyToMany
    @JoinTable(name = "brands_categories", joinColumns = @JoinColumn(name = "brand_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    @OrderBy("name asc")
    private Set<Category> categories = new HashSet<>();

    public Brand(Integer id) {
        this.id = id;
    }

    public String getLogoImagePath() {
        return Constants.S3_BASE_URI + (id == null || logo == null ? "/default.png" : "/brand" +
                "-photos/" + this.id + "/" + logo);
    }

    public void addCategory(Category category) {
        this.categories.add(category);
    }

}
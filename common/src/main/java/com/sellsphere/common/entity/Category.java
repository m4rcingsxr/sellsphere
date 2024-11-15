package com.sellsphere.common.entity;

import com.sellsphere.common.entity.constraints.RootCategoryIconRequired;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.proxy.HibernateProxy;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@RootCategoryIconRequired
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@Entity
@Table(name = "categories")
public class Category extends IdentifiedEntity {

    @NotNull(message = "Name is required")
    @Size(min = 1, max = 128, message = "Name must be between 1 and 128 characters")
    @Column(name = "name", length = 128, nullable = false, unique = true)
    private String name;

    @NotNull(message = "Alias is required")
    @Size(min = 1, max = 64, message = "Alias must be between 1 and 64 characters")
    @Column(name = "alias", length = 64, nullable = false, unique = true)
    private String alias;

    @Size(max = 128, message = "Image name must be between 1 and 128 characters")
    @Column(name = "image", length = 128)
    private String image;

    @Column(name = "enabled", nullable = false, columnDefinition = "TINYINT")
    private boolean enabled;

    @Size(max = 255, message = "All parent IDs must be between 1 and 255 characters")
    @Column(name = "all_parent_ids", length = 255, nullable = true)
    private String allParentIDs;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Category parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @OrderBy("name asc")
    private Set<Category> children = new HashSet<>();

    @ManyToMany(mappedBy = "categories", fetch = FetchType.LAZY)
    private Set<Brand> brands = new HashSet<>();

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private Set<Product> products = new HashSet<>();

    @OneToOne(mappedBy = "category", cascade = CascadeType.ALL)
    private CategoryIcon categoryIcon;

    @Transient
    public String getMainImagePath() {
        String basePath = Constants.S3_BASE_URI;
        if (id == null || image == null) {
            return basePath + "/default.png";
        }
        try {
            String encodedMainImage = URLEncoder.encode(image, "UTF-8");
            return basePath + "/category-photos/" + this.id + "/" + encodedMainImage;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Error encoding image URL", e);
        }
    }

    public Category(Category other) {
        this.id = other.id;
        this.name = other.name;
        this.alias = other.alias;
        this.image = other.image;
        this.enabled = other.enabled;
        this.allParentIDs = other.allParentIDs;
        this.parent = other.parent;
        this.children = other.children;
        this.categoryIcon = other.categoryIcon;
    }

    public Category(Integer id) {
        this.id = id;
    }

    public Category(Integer id, String name) {
        super(id);
        this.name = name;
    }

    @PreRemove
    private void removeCategoryFromBrands() {
        for (Brand brand : brands) {
            brand.getCategories().remove(this);
        }
    }

    public void addCategoryIcon(CategoryIcon categoryIcon) {
        categoryIcon.setCategory(this);
        this.categoryIcon = categoryIcon;
    }

    public void addChild(Category child) {
        child.setParent(this);
        this.children.add(child);
    }

    public void removeChild(Category child) {
        child.setParent(null);
        this.children.remove(child);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ?
                ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() :
                o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ?
                ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() :
                this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Category category = (Category) o;
        return getId() != null && Objects.equals(getId(), category.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ?
                ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }


}
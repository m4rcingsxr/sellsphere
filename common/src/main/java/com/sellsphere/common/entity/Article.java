package com.sellsphere.common.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "articles")
public class Article extends IdentifiedEntity {

    @NotBlank(message = "Title is required.")
    @Column(name = "title", nullable = false)
    private String title;

    @Size(max = 500, message = "Alias must not exceed 500 characters.")
    @Column(name = "alias", nullable = false, length = 500)
    private String alias;

    @NotBlank(message = "Content is required.")
    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User createdBy;

    @Column(name = "updated_time", nullable = false)
    private LocalDate updatedTime;

    @NotNull(message = "Published status is required.")
    @Column(name = "published", nullable = false)
    private Boolean published;

    @Column(name = "article_image", nullable = true)
    private String articleImage;

    @NotNull(message = "Article type is required.")
    @Column(name = "article_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ArticleType articleType;

    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "promotion_id", nullable = true)
    private Promotion promotion;

    public String getAlias() {
        if(alias == null) {
            return null;
        }
        try {
            return   URLEncoder.encode(alias, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Transient
    public String getMainImagePath() {
        String basePath = Constants.S3_BASE_URI;
        if (id == null || articleImage == null) {
            return basePath + "/default.png";
        }
        try {
            String encodedMainImage = URLEncoder.encode(articleImage, "UTF-8");
            return basePath + "/article-photos/" + this.id + "/" + encodedMainImage;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Error encoding image URL", e);
        }
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Article article = (Article) o;
        return getId() != null && Objects.equals(getId(), article.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }

}

package com.sellsphere.common.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDate;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "articles")
public class Article extends IdentifiedEntity {

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "alias", nullable = false, length = 500)
    private String alias;

    @Column(name = "content", nullable = false)
    @Lob
    private String content;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User createdBy;

    @Column(name = "updated_time", nullable = false)
    private LocalDate updatedTime;

    @Column(name = "published", nullable = false)
    private Boolean published;

    @Column(name = "article_image", nullable = true)
    private String articleImage;

    @Column(name = "article_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ArticleType articleType;

    @OneToOne(mappedBy = "article")
    private Promotion promotion;

    @Transient
    public String getMainImagePath() {
        return Constants.S3_BASE_URI + (id == null || articleImage == null ?
                "/default.png" :
                "/article-photos/" + this.id + "/" + articleImage);
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

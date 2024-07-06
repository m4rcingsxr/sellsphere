package com.sellsphere.common.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDate;
import java.util.Objects;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "reviews")
public class Review extends IdentifiedEntity {

    @Column(nullable = false, name = "rate")
    private Integer rate;

    @Column(length = 128, nullable = false, name = "headline")
    private String headline;

    @Column(length = 255, nullable = false, name = "cmt")
    private String comment;

    @Column(nullable = false, name = "review_time")
    private LocalDate reviewTime;

    @Column(nullable = false, name = "approved")
    private Boolean approved = false;

    @Column(name = "votes")
    private Integer votes;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Transient
    private boolean upVotedByCurrentCustomer;

    @Transient
    private boolean downVotedByCurrentCustomer;

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
        Review review = (Review) o;
        return getId() != null && Objects.equals(getId(), review.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ?
                ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}

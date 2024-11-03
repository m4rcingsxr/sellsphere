package com.sellsphere.common.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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

    /**
     * The rating given in the review.
     * Must be between 1 and 5.
     */
    @NotNull(message = "Rate is required")
    @Min(value = 1, message = "Rate must be at least 1")
    @Max(value = 5, message = "Rate must be at most 5")
    @Column(nullable = false, name = "rate")
    private Integer rate;

    /**
     * The headline of the review.
     * Must be between 1 and 128 characters.
     */
    @NotBlank(message = "Headline is required")
    @Size(max = 128, message = "Headline must be up to 128 characters")
    @Column(length = 128, nullable = false, name = "headline")
    private String headline;

    /**
     * The comment content of the review.
     * Must be between 1 and 255 characters.
     */
    @NotBlank(message = "Comment is required")
    @Size(max = 255, message = "Comment must be up to 255 characters")
    @Column(length = 255, nullable = false, name = "cmt")
    private String comment;

    /**
     * The time of the review.
     * Cannot be null and must be a date in the past or present.
     */
    @NotNull(message = "Review time is required")
    @PastOrPresent(message = "Review time must be in the past or present")
    @Column(nullable = false, name = "review_time")
    private LocalDate reviewTime;

    /**
     * Approval status of the review.
     * Cannot be null.
     */
    @NotNull(message = "Approval status is required")
    @Column(nullable = false, name = "approved")
    private Boolean approved = false;

    /**
     * The number of votes for the review.
     * Must be zero or a positive number.
     */
    @Min(value = 0, message = "Votes must be zero or positive")
    @Column(name = "votes")
    private Integer votes = 0;

    /**
     * The product associated with the review.
     * Must not be null.
     */
    @NotNull(message = "Product is required")
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    /**
     * The customer who submitted the review.
     * Must not be null.
     */
    @NotNull(message = "Customer is required")
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

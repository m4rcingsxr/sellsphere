package com.sellsphere.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDate;
import java.util.Objects;

@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "questions")
public class Question extends IdentifiedEntity {

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "question_content", nullable = false)
    private String questionContent;

    @Column(name = "ask_time", nullable = false)
    private LocalDate askTime;

    @Column(name = "answer_content", nullable = true)
    private String answerContent;

    @Column(name = "answerer", nullable = true)
    private String answerer;

    @Column(name = "answer_time", nullable = true)
    private LocalDate answerTime;

    @Column(name = "approval_status", nullable = true)
    private Boolean approvalStatus;

    @Column(name = "votes", nullable = true)
    private Integer votes;

    @Transient
    private boolean upVotedByCurrentCustomer;

    @Transient
    private boolean downVotedByCurrentCustomer;

    public Integer getVotes() {
        if(votes == null) {
            return 0;
        }
        return votes;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Question question = (Question) o;
        return getId() != null && Objects.equals(getId(), question.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}

package com.sellsphere.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "questions_votes")
public class QuestionVote extends IdentifiedEntity {

    private static final int VOTE_UP = 1;
    private static final int VOTE_DOWN = -1;

    @Column(name = "votes", nullable = false)
    private int votes;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    public void voteUp() {
        this.votes = VOTE_UP;
    }

    public void voteDown() {
        this.votes = VOTE_DOWN;
    }

    @Transient
    public boolean isUpvoted() {
        return this.votes == VOTE_UP;
    }

    @Transient
    public boolean isDownvoted() {
        return this.votes == VOTE_DOWN;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        QuestionVote that = (QuestionVote) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}

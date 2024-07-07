package com.sellsphere.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "reviews_votes")
public class ReviewVote extends IdentifiedEntity {

    private static final int VOTE_UP = 1;
    private static final int VOTE_DOWN = -1;

    @Column(name = "votes", nullable = false)
    private int votes;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

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

}
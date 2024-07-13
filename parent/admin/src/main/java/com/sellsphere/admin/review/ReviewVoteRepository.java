package com.sellsphere.admin.review;

import com.sellsphere.common.entity.Review;
import com.sellsphere.common.entity.ReviewVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewVoteRepository extends JpaRepository<ReviewVote, Integer> {

    void deleteAllByReview(Review review);

}

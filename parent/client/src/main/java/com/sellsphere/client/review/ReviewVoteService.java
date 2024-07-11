package com.sellsphere.client.review;

import com.sellsphere.client.product.ProductRepository;
import com.sellsphere.common.entity.*;
import com.sellsphere.common.entity.payload.VoteResultDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Transactional
@RequiredArgsConstructor
@Service
public class ReviewVoteService {

    private final ReviewVoteRepository reviewVoteRepository;
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    public VoteResultDTO doVote(Integer reviewId, Customer customer, VoteType voteType) {
        Review review = null;
        try {
            review = reviewRepository.findById(reviewId).orElseThrow(ReviewNotFoundException::new);
        } catch(ReviewNotFoundException e) {
            return VoteResultDTO.fail("Review id [" + reviewId + "] does not exist.");
        }

        Optional<ReviewVote> voteOpt = reviewVoteRepository.findByReviewIdAndCustomerId(reviewId, customer.getId());
        ReviewVote vote;

        if(voteOpt.isPresent()) {
            vote = voteOpt.get();
            if (vote.isUpvoted() && voteType.equals(VoteType.UP) || vote.isDownvoted() && voteType.equals(VoteType.DOWN)) {
                return undoVote(reviewId, vote, voteType);
            } else if (vote.isUpvoted() && voteType.equals(VoteType.DOWN)) {
                vote.voteDown();
            } else if (vote.isDownvoted() && voteType.equals(VoteType.UP)) {
                vote.voteUp();
            }
        } else {
            vote = new ReviewVote();
            vote.setCustomer(customer);
            vote.setReview(review);

            if(voteType.equals(VoteType.UP)) {
                vote.voteUp();
            } else {
                vote.voteDown();
            }
        }

        reviewVoteRepository.save(vote);
        reviewRepository.updateVoteCount(reviewId);
        Integer voteCount = reviewRepository.findVotesByReviewId(reviewId);

        return VoteResultDTO.success("You have successfully voted " + voteType + " that review.", voteCount == null ? 0 : voteCount);
    }

    public VoteResultDTO undoVote(Integer reviewId, ReviewVote vote, VoteType voteType) {
        reviewVoteRepository.delete(vote);
        reviewRepository.updateVoteCount(reviewId);

        Integer voteCount = reviewRepository.findVotesByReviewId(reviewId);
        return VoteResultDTO.success("You have un-voted " + voteType + " that review.", voteCount == null ? 0 : voteCount);
    }

    public void markReviewsVotedForProductByCustomer(List<Review> reviewList, Product product, Customer customer) {
        if(customer == null) {
            return;
        }
        List<ReviewVote> listVotes = reviewVoteRepository.findAllByProductAndCustomer(product.getId(), customer.getId());

        for (ReviewVote reviewVote : listVotes) {
            Review votedReview = reviewVote.getReview();

            if (reviewList.contains(votedReview)) {
                int index = reviewList.indexOf(votedReview);
                Review review = reviewList.get(index);

                if (reviewVote.isUpvoted()) {
                    review.setUpVotedByCurrentCustomer(true);
                } else if (reviewVote.isDownvoted()){
                    review.setDownVotedByCurrentCustomer(true);
                }

            }
        }
    }

}

package com.sellsphere.client.question;

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
public class QuestionVoteService {

    private final QuestionVoteRepository questionVoteRepository;
    private final QuestionRepository questionRepository;

    public VoteResultDTO doVote(Integer questionId, Customer customer, VoteType voteType) {
        Question question = null;

        try {
            question = questionRepository.findById(questionId).orElseThrow(QuestionNotFoundException::new);
        } catch (QuestionNotFoundException e) {
            return VoteResultDTO.fail("Review id [" + questionId + "] does not exist.");
        }

        Optional<QuestionVote> voteOpt = questionVoteRepository.findByQuestionIdAndCustomerId(questionId, customer.getId());
        QuestionVote vote;

        if (voteOpt.isPresent()) {
            vote = voteOpt.get();
            if (vote.isUpvoted() && voteType.equals(VoteType.UP) || vote.isDownvoted() && voteType.equals(VoteType.DOWN)) {
                return undoVote(questionId, vote, voteType);
            } else if (vote.isUpvoted() && voteType.equals(VoteType.DOWN)) {
                vote.voteDown();
            } else if (vote.isDownvoted() && voteType.equals(VoteType.UP)) {
                vote.voteUp();
            }
        } else {
            vote = new QuestionVote();
            vote.setCustomer(customer);
            vote.setQuestion(question);

            if (voteType.equals(VoteType.UP)) {
                vote.voteUp();
            } else {
                vote.voteDown();
            }
        }

        questionVoteRepository.save(vote);
        questionRepository.updateVoteCount(questionId);
        Integer voteCount = questionRepository.findVotesByQuestionId(questionId);

        return VoteResultDTO.success("You have successfully voted " + voteType + " that question.", voteCount == null ? 0 : voteCount);
    }

    public VoteResultDTO undoVote(Integer questionId, QuestionVote vote, VoteType voteType) {
        questionVoteRepository.delete(vote);
        questionRepository.updateVoteCount(questionId);

        Integer voteCount = questionRepository.findVotesByQuestionId(questionId);
        return VoteResultDTO.success("You have un-voted " + voteType + " that question.", voteCount == null ? 0 : voteCount);
    }

    public void markQuestionVotedForProductByCustomer(List<Question> questionList, Product product, Customer customer) {
        if (customer == null) {
            return;
        }
        List<QuestionVote> listVotes = questionVoteRepository.findAllByProductAndCustomer(product.getId(), customer.getId());

        for (QuestionVote questionVote : listVotes) {
            Question votedQuestion = questionVote.getQuestion();

            if (questionList.contains(votedQuestion)) {
                int index = questionList.indexOf(votedQuestion);
                Question question = questionList.get(index);

                if (questionVote.isUpvoted()) {
                    question.setUpVotedByCurrentCustomer(true);
                } else if (questionVote.isDownvoted()) {
                    question.setDownVotedByCurrentCustomer(true);
                }

            }
        }
    }
}

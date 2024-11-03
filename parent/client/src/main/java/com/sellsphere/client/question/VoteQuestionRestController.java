package com.sellsphere.client.question;

import com.sellsphere.client.customer.CustomerService;
import com.sellsphere.common.entity.Customer;
import com.sellsphere.common.entity.CustomerNotFoundException;
import com.sellsphere.common.entity.VoteType;
import com.sellsphere.common.entity.payload.VoteResultDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RequiredArgsConstructor
@RestController
public class VoteQuestionRestController {

    private final CustomerService customerService;
    private final QuestionVoteService questionVoteService;

    @PostMapping("/vote_question/{id}/{voteType}")
    public VoteResultDTO voteQuestion(@PathVariable("id") Integer reviewId,
                                    @PathVariable("voteType") String voteType,
                                    Principal principal) throws CustomerNotFoundException {
        Customer customer = getAuthenticatedCustomer(principal);
        if(customer == null) {
            return VoteResultDTO.fail("You must login to vote the review.");
        }

        VoteType type = VoteType.valueOf(voteType.toUpperCase());
        return questionVoteService.doVote(reviewId, customer, type);
    }

    private Customer getAuthenticatedCustomer(Principal principal) throws CustomerNotFoundException {
        if (principal != null) {
            return customerService.getByEmail(principal.getName());
        } else {
            return null;
        }
    }

}

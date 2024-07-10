package com.sellsphere.common.entity.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VoteResultDTO {
    private String message;
    private Boolean successful;
    private int voteCount;

    public static VoteResultDTO fail(String message) {
        return new VoteResultDTO(message, false, 0);
    }

    public static VoteResultDTO success(String message, int voteCount) {
        return new VoteResultDTO(message, true, voteCount);
    }
}

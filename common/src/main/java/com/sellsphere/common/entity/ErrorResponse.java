package com.sellsphere.common.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponse {
    private String message;
    private int status;
    private long timestamp = System.currentTimeMillis();

    public ErrorResponse(String message, int status) {
        this.message = message;
        this.status = status;
    }
}

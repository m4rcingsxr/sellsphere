package com.sellsphere.common.entity;

public class QuestionNotFoundException extends Throwable {
    public QuestionNotFoundException() {
        super("Question not found.");
    }

    public QuestionNotFoundException(String message) {
        super(message);
    }
}

package com.sellsphere.common.entity;

public class ArticleNotFoundException extends Exception {

    public ArticleNotFoundException() {
        super("Article not found");
    }

    public ArticleNotFoundException(String message) {
        super(message);
    }
}

package com.sellsphere.client.article;

public class ArticleNotFoundException extends Throwable {

    public ArticleNotFoundException() {
        super("Article not found.");
    }

    public ArticleNotFoundException(String message) {
        super(message);
    }

}

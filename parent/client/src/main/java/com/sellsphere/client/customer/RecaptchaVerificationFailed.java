package com.sellsphere.client.customer;

public class RecaptchaVerificationFailed extends Exception {

    private String redirectUrl;

    public RecaptchaVerificationFailed(String redirectUrl, String message) {
        super(message);
        this.redirectUrl = redirectUrl;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }
}

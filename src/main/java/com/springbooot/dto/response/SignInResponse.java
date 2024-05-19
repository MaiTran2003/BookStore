package com.springbooot.dto.response;

public class SignInResponse {
    private String message;

    // Constructor

    public SignInResponse() {
    }

    public SignInResponse(String message) {
        this.message = message;
    }

    // Getter và setter cho trường message
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

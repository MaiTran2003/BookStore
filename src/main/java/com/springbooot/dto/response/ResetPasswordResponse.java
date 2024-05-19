package com.springbooot.dto.response;

public class ResetPasswordResponse {
    private String message;

    // Constructors
    public ResetPasswordResponse() {
    }

    public ResetPasswordResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

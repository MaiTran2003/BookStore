package com.springbooot.dto.response;

public class ForgotPasswordResponse {
    private String message;
    private String email;

    // Constructors
    public ForgotPasswordResponse() {
    }

    public ForgotPasswordResponse(String message, String email) {
        this.message = message;
        this.email = email;
    }

    // Getters and setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

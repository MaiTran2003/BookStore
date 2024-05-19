package com.springbooot.dto.response;

public class ChangeEmailResponse {
    private String message;

    // Constructors
    public ChangeEmailResponse() {
    }

    public ChangeEmailResponse(String message) {
        this.message = message;
    }

    // Getter and setter for message
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

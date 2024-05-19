package com.springbooot.dto.response;

public class LogoutResponse {
    private String message;

    public LogoutResponse() {
        // Default constructor
    }

    public LogoutResponse(String message) {
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

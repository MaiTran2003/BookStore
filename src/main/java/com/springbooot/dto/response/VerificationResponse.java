package com.springbooot.dto.response;

public class VerificationResponse {
    private String message;
    private boolean success;

    public VerificationResponse() {
    }

    public VerificationResponse(String message, boolean success) {
        this.message = message;
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}

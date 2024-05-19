package com.springbooot.dto.response;

public class UpdateBookResponse {
    private String message;

    public UpdateBookResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

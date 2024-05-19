package com.springbooot.dto.response;

public class SignupResponse {
    private String errorMessage;
    private UserResponse userResponse;

    public SignupResponse() {
    }

    public SignupResponse(String errorMessage) {
        super();
        this.errorMessage = errorMessage;
    }

    public SignupResponse(String errorMessage, UserResponse userResponse) {
        this.errorMessage = errorMessage;
        this.userResponse = userResponse;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public UserResponse getUserResponse() {
        return userResponse;
    }

    public void setUserResponse(UserResponse userResponse) {
        this.userResponse = userResponse;
    }
}

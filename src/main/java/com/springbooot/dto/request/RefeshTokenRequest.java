package com.springbooot.dto.request;

public class RefeshTokenRequest {
    private String token;

    public RefeshTokenRequest() {
    }

    public RefeshTokenRequest(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}

package com.springbooot.dto.request;

public class SignOutRequest {
    private String token;
    private String email;
    private String password;

    public SignOutRequest() {
    }

    public SignOutRequest(String password, String token, String email) {
        super();
        this.token = token;
        this.email = email;
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}

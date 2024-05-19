package com.springbooot.dto.request;

public class ChangeEmailRequest {
    private String oldEmail;
    private String newEmail;

    public ChangeEmailRequest() {
    }

    public ChangeEmailRequest(String oldEmail, String newEmail) {
        super();
        this.oldEmail = oldEmail;
        this.newEmail = newEmail;
    }

    public String getOldEmail() {
        return oldEmail;
    }

    public void setOldEmail(String oldEmail) {
        this.oldEmail = oldEmail;
    }

    public String getNewEmail() {
        return newEmail;
    }

    public void setNewEmail(String newEmail) {
        this.newEmail = newEmail;
    }

}

package com.rednet.sessionservice.entity;

import java.io.Serializable;

public class Session implements Serializable {
    private String userID;
    private String[] roles;
    private String accessToken;
    private String refreshToken;

    private Session() {}
    public Session(String userID, String[] roles, String accessToken, String refreshToken) {
        this.userID = userID;
        this.roles = roles;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String[] getRoles() {
        return roles;
    }

    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}

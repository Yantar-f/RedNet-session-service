package com.rednet.sessionservice.entity;


public class SessionKey {
    private String userID;

    private String sessionPostfix;


    public SessionKey(String userID, String sessionPostfix) {
        this.userID = userID;
        this.sessionPostfix = sessionPostfix;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getSessionPostfix() {
        return sessionPostfix;
    }

    public void setSessionPostfix(String sessionPostfix) {
        this.sessionPostfix = sessionPostfix;
    }
}

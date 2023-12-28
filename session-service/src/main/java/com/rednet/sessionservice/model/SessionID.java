package com.rednet.sessionservice.model;

public class SessionID {
    private String userID;
    private String sessionKey;

    public SessionID(String userID, String sessionKey) {
        this.userID = userID;
        this.sessionKey = sessionKey;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    @Override
    public int hashCode() {
        return userID.hashCode() * sessionKey.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;

        SessionID sessionID = (SessionID) obj;

        return userID.equals(sessionID.userID) && sessionKey.equals(sessionID.sessionKey);
    }
}

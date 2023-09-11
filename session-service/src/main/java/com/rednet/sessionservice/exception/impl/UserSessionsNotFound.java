package com.rednet.sessionservice.exception.impl;

public class UserSessionsNotFound extends RuntimeException {
    public UserSessionsNotFound(String userID) {
        super("Sessions of user " + userID + " not found");
    }
}

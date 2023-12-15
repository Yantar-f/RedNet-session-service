package com.rednet.sessionservice.exception.impl;

public class UserSessionsRemovingException extends RuntimeException {

    public UserSessionsRemovingException(String userID) {
        super("error removing session of user " + userID);
    }
}

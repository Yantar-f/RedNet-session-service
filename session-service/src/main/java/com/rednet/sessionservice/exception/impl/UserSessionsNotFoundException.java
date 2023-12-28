package com.rednet.sessionservice.exception.impl;

public class UserSessionsNotFoundException extends RuntimeException {
    public UserSessionsNotFoundException(String userID) {
        super("Sessions of user " + userID + " not found");
    }
}

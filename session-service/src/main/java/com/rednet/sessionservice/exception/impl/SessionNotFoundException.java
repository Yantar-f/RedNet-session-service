package com.rednet.sessionservice.exception.impl;

public class SessionNotFoundException extends RuntimeException {
    public SessionNotFoundException(String sessionID) {
        super("session " + sessionID + " not found");
    }
}

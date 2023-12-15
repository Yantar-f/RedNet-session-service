package com.rednet.sessionservice.exception.impl;

public class SessionRemovingException extends RuntimeException {
    public SessionRemovingException(String sessionID) {
        super("error removing session " + sessionID);
    }
}

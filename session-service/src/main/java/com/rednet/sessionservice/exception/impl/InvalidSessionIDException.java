package com.rednet.sessionservice.exception.impl;

public class InvalidSessionIDException extends RuntimeException {
    public InvalidSessionIDException(String sessionID) {
        super("Invalid session id: " + sessionID);
    }
}

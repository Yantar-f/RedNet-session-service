package com.rednet.sessionservice.exception.impl;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String token) {
        super("invalid token: " + token);
    }
}

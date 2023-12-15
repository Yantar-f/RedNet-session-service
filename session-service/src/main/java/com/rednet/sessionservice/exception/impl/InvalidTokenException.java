package com.rednet.sessionservice.exception.impl;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException() {
        super("invalid token");
    }
}

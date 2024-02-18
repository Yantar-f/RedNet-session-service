package com.rednet.sessionservice.exception.impl;

public class ServerErrorException extends RuntimeException {
    private ServerErrorException(String message) {
        super(message);
    }
}

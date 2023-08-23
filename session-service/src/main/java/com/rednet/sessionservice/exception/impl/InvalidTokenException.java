package com.rednet.sessionservice.exception.impl;

import com.rednet.sessionservice.exception.BadRequestException;

import java.util.List;

public class InvalidTokenException extends BadRequestException {
    public InvalidTokenException() {
        super(List.of("invalid token"));
    }
}

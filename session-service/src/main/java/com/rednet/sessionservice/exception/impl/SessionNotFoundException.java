package com.rednet.sessionservice.exception.impl;

import com.rednet.sessionservice.exception.BadRequestException;

import java.util.List;

public class SessionNotFoundException extends BadRequestException {
    public SessionNotFoundException(String sessionID) {
        super(List.of("session" + sessionID + "not found"));
    }
}

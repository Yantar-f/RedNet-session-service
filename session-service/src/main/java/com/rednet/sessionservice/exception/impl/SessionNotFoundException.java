package com.rednet.sessionservice.exception.impl;

import com.rednet.sessionservice.exception.NotFoundException;

import java.util.List;

public class SessionNotFoundException extends NotFoundException {
    public SessionNotFoundException(String sessionID) {
        super(List.of("session " + sessionID + " not found"));
    }
}

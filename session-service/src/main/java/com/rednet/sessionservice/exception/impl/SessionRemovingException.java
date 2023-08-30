package com.rednet.sessionservice.exception.impl;

import com.rednet.sessionservice.exception.BadRequestException;

import java.util.List;

public class SessionRemovingException extends BadRequestException {
    public SessionRemovingException(String sessionID) {
        super(List.of("error removing session " + sessionID));
    }
}

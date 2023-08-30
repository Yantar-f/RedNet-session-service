package com.rednet.sessionservice.exception.impl;

import com.rednet.sessionservice.exception.BadRequestException;

import java.util.List;

public class UserSessionsRemovingException extends BadRequestException {

    public UserSessionsRemovingException(String userID) {
        super(List.of("error removing session of user " + userID));
    }
}

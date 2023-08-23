package com.rednet.sessionservice.exception.impl;

import com.rednet.sessionservice.exception.BadRequestException;

import java.util.List;

public class UserSessionsNotFound extends BadRequestException {
    public UserSessionsNotFound(String userID) {
        super(List.of("Sessions of user " + userID + " not found"));
    }
}

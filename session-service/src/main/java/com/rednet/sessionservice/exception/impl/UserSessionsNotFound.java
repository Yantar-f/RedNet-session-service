package com.rednet.sessionservice.exception.impl;

import com.rednet.sessionservice.exception.NotFoundException;

import java.util.List;

public class UserSessionsNotFound extends NotFoundException {
    public UserSessionsNotFound(String userID) {
        super(List.of("Sessions of user " + userID + " not found"));
    }
}

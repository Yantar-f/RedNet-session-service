package com.rednet.sessionservice.payload.request;

public record CreateSessionRequestBody(String userID, String[] roles) {}

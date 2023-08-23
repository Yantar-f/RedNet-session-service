package com.rednet.sessionservice.service;

import com.rednet.sessionservice.entity.Session;
import com.rednet.sessionservice.payload.request.CreateSessionRequestBody;

import java.util.List;

public interface SessionService {
    Session createSession(CreateSessionRequestBody requestBody);
    Session getSession(String sessionID);
    List<Session> getSessionsByUserID(String userID);
    Session refreshSession(String refreshToken);
    boolean deleteSession(String refreshToken);
    boolean deleteSessionsByUserID(String userID);
}

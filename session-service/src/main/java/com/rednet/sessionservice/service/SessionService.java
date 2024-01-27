package com.rednet.sessionservice.service;

import com.rednet.sessionservice.entity.Session;
import com.rednet.sessionservice.model.SessionCreationData;

import java.util.List;

public interface SessionService {
    Session         createSession           (SessionCreationData creationData);
    Session         getSession              (String sessionID);
    List<Session>   getSessionsByUserID     (String userID);
    Session         refreshSession          (String refreshToken);
    void            deleteSession           (String refreshToken);
    void            deleteSessionsByUserID  (String userID);
}

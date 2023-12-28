package com.rednet.sessionservice.util;

import com.rednet.sessionservice.model.SessionID;

public interface SessionIDShaper {
    SessionID   generate    (String userID);
    SessionID   parse       (String sessionID);
    String      convert     (SessionID sessionID);
}

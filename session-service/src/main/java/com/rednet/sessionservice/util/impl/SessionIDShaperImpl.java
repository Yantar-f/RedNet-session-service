package com.rednet.sessionservice.util.impl;

import com.rednet.sessionservice.exception.impl.InvalidSessionIDException;
import com.rednet.sessionservice.model.SessionID;
import com.rednet.sessionservice.util.SessionIDShaper;
import com.rednet.sessionservice.util.SessionKeyGenerator;
import org.springframework.stereotype.Component;

@Component
public class SessionIDShaperImpl implements SessionIDShaper {
    private final char separator = '.';
    private final SessionKeyGenerator sessionKeyGenerator;
    private final int minSessionIDLength;

    public SessionIDShaperImpl(SessionKeyGenerator sessionKeyGenerator) {
        this.sessionKeyGenerator = sessionKeyGenerator;
        minSessionIDLength = sessionKeyGenerator.getKeyLength() + 2;
    }

    @Override
    public SessionID generate(String userID) {
        return new SessionID(userID, sessionKeyGenerator.generate());
    }

    @Override
    public SessionID parse(String sessionID) {
        int separatorIndex = computeSeparatorIndex(sessionID);
        String userID = extractUserID(sessionID, separatorIndex);
        String sessionKey = extractSessionKey(sessionID, separatorIndex);

        return new SessionID(userID, sessionKey);
    }

    private int computeSeparatorIndex(String sessionID) {
        if (sessionID.length() < minSessionIDLength)
            throw new InvalidSessionIDException(sessionID);

        int separatorIndex = sessionID.length() - 1 - sessionKeyGenerator.getKeyLength();

        if (sessionID.charAt(separatorIndex) != separator)
            throw new InvalidSessionIDException(sessionID);

        return separatorIndex;
    }

    private String extractSessionKey(String sessionID, int separatorIndex) {
        return sessionID.substring(separatorIndex + 1);
    }

    private String extractUserID(String sessionID, int separatorIndex) {
        return sessionID.substring(0,separatorIndex);
    }

    @Override
    public String convert(SessionID sessionID) {
        return sessionID.getUserID() + separator + sessionID.getSessionKey();
    }
}

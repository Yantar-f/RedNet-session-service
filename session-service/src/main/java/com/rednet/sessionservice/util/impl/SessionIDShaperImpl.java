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

    public SessionIDShaperImpl(SessionKeyGenerator sessionKeyGenerator) {
        this.sessionKeyGenerator = sessionKeyGenerator;
    }

    @Override
    public SessionID generate(String userID) {
        return new SessionID(userID, sessionKeyGenerator.generate());
    }

    @Override
    public SessionID parse(String sessionID) {
        if (sessionID.length() < sessionKeyGenerator.getKeyLength() + 2)
            throw new InvalidSessionIDException(sessionID);

        int separatorIndex = sessionID.length() - 1 - sessionKeyGenerator.getKeyLength();

        if (sessionID.charAt(separatorIndex) != separator)
            throw new InvalidSessionIDException(sessionID);

        return new SessionID(
                sessionID.substring(0,separatorIndex),
                sessionID.substring(separatorIndex + 1)
        );
    }

    @Override
    public String convert(SessionID sessionID) {
        return sessionID.getUserID() + separator + sessionID.getSessionKey();
    }
}

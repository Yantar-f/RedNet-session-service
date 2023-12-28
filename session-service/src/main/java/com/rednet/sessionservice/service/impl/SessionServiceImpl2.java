package com.rednet.sessionservice.service.impl;

import com.rednet.sessionservice.entity.Session;
import com.rednet.sessionservice.exception.impl.InvalidSessionIDException;
import com.rednet.sessionservice.exception.impl.SessionNotFoundException;
import com.rednet.sessionservice.exception.impl.UserSessionsNotFoundException;
import com.rednet.sessionservice.model.SessionID;
import com.rednet.sessionservice.model.TokenClaims;
import com.rednet.sessionservice.repository.SessionRepository;
import com.rednet.sessionservice.service.SessionService;
import com.rednet.sessionservice.service.SessionTokenService;
import com.rednet.sessionservice.util.SessionIDShaper;
import com.rednet.sessionservice.util.TokenIDGenerator;

import java.time.Instant;
import java.util.List;

public class SessionServiceImpl2 implements SessionService {
    private final SessionRepository     sessionRepository;
    private final TokenIDGenerator      tokenIDGenerator;
    private final SessionTokenService   sessionTokenService;
    private final SessionIDShaper       sessionIDShaper;

    public SessionServiceImpl2(
            SessionRepository sessionRepository,
            TokenIDGenerator tokenIDGenerator,
            SessionTokenService sessionTokenService,
            SessionIDShaper sessionIDShaper
    ) {
        this.sessionRepository = sessionRepository;
        this.tokenIDGenerator = tokenIDGenerator;
        this.sessionTokenService = sessionTokenService;
        this.sessionIDShaper = sessionIDShaper;
    }

    @Override
    public Session createSession(String userID, String[] roles) {
        SessionID sessionIDModel = sessionIDShaper.generate(userID);
        String tokenID = tokenIDGenerator.generate();
        TokenClaims tokenClaims = new TokenClaims(userID, sessionIDShaper.convert(sessionIDModel), tokenID, roles);

        return sessionRepository.insert(new Session(
                userID,
                sessionIDModel.getSessionKey(),
                Instant.now(),
                roles,
                sessionTokenService.generateAccessToken(tokenClaims),
                sessionTokenService.generateRefreshToken(tokenClaims),
                tokenID
        ));
    }

    @Override
    public Session getSession(String sessionID) {
        try {
            SessionID sessionIDModel = sessionIDShaper.parse(sessionID);

            return sessionRepository
                    .findByID(sessionIDModel)
                    .orElseThrow(() -> new SessionNotFoundException(sessionID));
        } catch (InvalidSessionIDException e) {
            throw new SessionNotFoundException(sessionID);
        }
    }

    @Override
    public List<Session> getSessionsByUserID(String userID) {
        List<Session> sessions = sessionRepository.findAllByUserID(userID);

        if (sessions.isEmpty()) throw new UserSessionsNotFoundException(userID);

        return sessions;
    }

    @Override
    public Session refreshSession(String refreshToken) {
        return null;
    }

    @Override
    public void deleteSession(String refreshToken) {

    }

    @Override
    public void deleteSessionsByUserID(String userID) {

    }
}

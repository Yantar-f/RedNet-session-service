package com.rednet.sessionservice.service.impl;

import com.rednet.sessionservice.entity.Session;
import com.rednet.sessionservice.exception.impl.*;
import com.rednet.sessionservice.model.SessionID;
import com.rednet.sessionservice.model.TokenClaims;
import com.rednet.sessionservice.repository.SessionRepository;
import com.rednet.sessionservice.service.SessionService;
import com.rednet.sessionservice.service.TokenService;
import com.rednet.sessionservice.util.SessionIDShaper;
import com.rednet.sessionservice.util.TokenIDGenerator;

import java.time.Instant;
import java.util.List;

public class SessionServiceImpl2 implements SessionService {
    private final SessionRepository sessionRepository;
    private final TokenIDGenerator  tokenIDGenerator;
    private final TokenService      tokenService;
    private final SessionIDShaper   sessionIDShaper;

    public SessionServiceImpl2(
            SessionRepository   sessionRepository,
            TokenIDGenerator    tokenIDGenerator,
            TokenService        tokenService,
            SessionIDShaper     sessionIDShaper
    ) {
        this.sessionRepository  = sessionRepository;
        this.tokenIDGenerator   = tokenIDGenerator;
        this.tokenService       = tokenService;
        this.sessionIDShaper    = sessionIDShaper;
    }

    @Override
    public Session createSession(String userID, String[] roles) {
        SessionID sessionID = sessionIDShaper.generate(userID);
        String tokenID = tokenIDGenerator.generate();
        TokenClaims claims = new TokenClaims(userID, sessionIDShaper.convert(sessionID), tokenID, roles);

        return sessionRepository.insert(new Session(
                userID,
                sessionID.getSessionKey(),
                Instant.now(),
                roles,
                tokenService.generateAccessToken(claims),
                tokenService.generateRefreshToken(claims),
                tokenID
        ));
    }

    @Override
    public Session getSession(String convertedSessionID) {
        try {
            SessionID sessionID = sessionIDShaper.parse(convertedSessionID);

            return sessionRepository
                    .findByID(sessionID)
                    .orElseThrow(() -> new SessionNotFoundException(convertedSessionID));
        } catch (InvalidSessionIDException e) {
            throw new SessionNotFoundException(convertedSessionID);
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
        try {
            TokenClaims claims = tokenService.parseRefreshToken(refreshToken);
            SessionID sessionID = sessionIDShaper.parse(claims.getSessionID());

            Session session = sessionRepository
                    .findByID(sessionID)
                    .orElseThrow(() -> new InvalidTokenException(refreshToken));

            if ( ! session.getTokenID().equals(claims.getTokenID())) throw new InvalidTokenException(refreshToken);

            claims.setTokenID(tokenIDGenerator.generate());

            session.setAccessToken(tokenService.generateAccessToken(claims));
            session.setRefreshToken(tokenService.generateRefreshToken(claims));
            session.setCreatedAt(Instant.now());
            session.setTokenID(claims.getTokenID());

            return sessionRepository.insert(session);
        } catch (InvalidSessionIDException e) {
            throw new InvalidTokenException(refreshToken);
        }
    }

    @Override
    public void deleteSession(String refreshToken) {
        try {
            TokenClaims claims = tokenService.parseRefreshToken(refreshToken);
            SessionID sessionID = sessionIDShaper.parse(claims.getSessionID());

            Session session = sessionRepository
                    .findByID(sessionID)
                    .orElseThrow(() -> new InvalidTokenException(refreshToken));

            if ( ! session.getTokenID().equals(claims.getTokenID())) throw new InvalidTokenException(refreshToken);

            sessionRepository.deleteByID(sessionID);
        } catch (InvalidSessionIDException e) {
            throw new InvalidTokenException(refreshToken);
        }
    }

    @Override
    public void deleteSessionsByUserID(String userID) {
        if (sessionRepository.existsByUserID(userID)) {
            if ( ! sessionRepository.deleteAllByUserID(userID)) throw new UserSessionsRemovingException(userID);
        } else {
            throw new UserSessionsNotFoundException(userID);
        }
    }
}

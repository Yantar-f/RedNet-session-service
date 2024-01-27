package com.rednet.sessionservice.service.impl;

import com.rednet.sessionservice.entity.Session;
import com.rednet.sessionservice.exception.impl.InvalidSessionIDException;
import com.rednet.sessionservice.exception.impl.InvalidTokenException;
import com.rednet.sessionservice.exception.impl.SessionNotFoundException;
import com.rednet.sessionservice.exception.impl.SessionRemovingException;
import com.rednet.sessionservice.exception.impl.UserSessionsNotFoundException;
import com.rednet.sessionservice.exception.impl.UserSessionsRemovingException;
import com.rednet.sessionservice.model.SessionCreationData;
import com.rednet.sessionservice.model.SessionID;
import com.rednet.sessionservice.model.TokenClaims;
import com.rednet.sessionservice.repository.SessionRepository;
import com.rednet.sessionservice.service.SessionService;
import com.rednet.sessionservice.util.SessionIDShaper;
import com.rednet.sessionservice.util.TokenIDGenerator;
import com.rednet.sessionservice.util.TokenUtil;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class SessionServiceImpl implements SessionService {
    private final SessionRepository sessionRepository;
    private final TokenIDGenerator tokenIDGenerator;
    private final TokenUtil tokenUtil;
    private final SessionIDShaper sessionIDShaper;

    public SessionServiceImpl(SessionRepository sessionRepository,
                              TokenIDGenerator tokenIDGenerator,
                              TokenUtil tokenUtil,
                              SessionIDShaper sessionIDShaper) {
        this.sessionRepository = sessionRepository;
        this.tokenIDGenerator = tokenIDGenerator;
        this.tokenUtil = tokenUtil;
        this.sessionIDShaper = sessionIDShaper;
    }

    @Override
    public Session createSession(SessionCreationData creationData) {
        SessionID sessionID = sessionIDShaper.generate(creationData.userID());
        String tokenID = tokenIDGenerator.generate();
        String convertedSessionID = sessionIDShaper.convert(sessionID);
        TokenClaims claims = new TokenClaims(creationData.userID(), convertedSessionID, tokenID, creationData.roles());
        String accessToken = tokenUtil.generateAccessToken(claims);
        String refreshToken = tokenUtil.generateRefreshToken(claims);

        return sessionRepository.insert(new Session(
                creationData.userID(),
                sessionID.getSessionKey(),
                Instant.now(),
                creationData.roles(),
                accessToken,
                refreshToken,
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

        if (sessions.isEmpty())
            throw new UserSessionsNotFoundException(userID);

        return sessions;
    }

    @Override
    public Session refreshSession(String refreshToken) {
        try {
            TokenClaims claims = tokenUtil.parseRefreshToken(refreshToken);
            SessionID sessionID = sessionIDShaper.parse(claims.getSessionID());

            Session session = sessionRepository
                    .findByID(sessionID)
                    .orElseThrow(() -> new InvalidTokenException(refreshToken));

            if ( ! session.getTokenID().equals(claims.getTokenID()))
                throw new InvalidTokenException(refreshToken);

            claims.setTokenID(tokenIDGenerator.generate());

            session.setAccessToken(tokenUtil.generateAccessToken(claims));
            session.setRefreshToken(tokenUtil.generateRefreshToken(claims));
            session.setTokenID(claims.getTokenID());
            session.setCreatedAt(Instant.now());

            return sessionRepository.insert(session);
        } catch (InvalidSessionIDException e) {
            throw new InvalidTokenException(refreshToken);
        }
    }

    @Override
    public void deleteSession(String refreshToken) {
        try {
            TokenClaims claims = tokenUtil.parseRefreshToken(refreshToken);
            SessionID sessionID = sessionIDShaper.parse(claims.getSessionID());

            Session session = sessionRepository
                    .findByID(sessionID)
                    .orElseThrow(() -> new InvalidTokenException(refreshToken));

            if (! session.getTokenID().equals(claims.getTokenID()))
                throw new InvalidTokenException(refreshToken);

            if (! sessionRepository.deleteByID(sessionID))
                throw new SessionRemovingException(claims.getSessionID());
        } catch (InvalidSessionIDException e) {
            throw new InvalidTokenException(refreshToken);
        }
    }

    @Override
    public void deleteSessionsByUserID(String userID) {
        if (sessionRepository.existsByUserID(userID)) {
            if (! sessionRepository.deleteAllByUserID(userID))
                throw new UserSessionsRemovingException(userID);
        } else {
            throw new UserSessionsNotFoundException(userID);
        }
    }
}

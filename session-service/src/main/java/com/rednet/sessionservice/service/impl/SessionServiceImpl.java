package com.rednet.sessionservice.service.impl;

import com.rednet.sessionservice.entity.Session;
import com.rednet.sessionservice.model.SessionID;
import com.rednet.sessionservice.exception.impl.InvalidTokenException;
import com.rednet.sessionservice.exception.impl.SessionNotFoundException;
import com.rednet.sessionservice.exception.impl.SessionRemovingException;
import com.rednet.sessionservice.exception.impl.UserSessionsNotFoundException;
import com.rednet.sessionservice.exception.impl.UserSessionsRemovingException;
import com.rednet.sessionservice.repository.SessionRepository;
import com.rednet.sessionservice.service.SessionService;
import com.rednet.sessionservice.util.JwtUtil;
import com.rednet.sessionservice.util.SessionKeyGenerator;
import com.rednet.sessionservice.util.TokenIDGenerator;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class SessionServiceImpl implements SessionService {
    private final SessionRepository     sessionRepository;
    private final JwtUtil               jwtUtil;
    private final SessionKeyGenerator   sessionKeyGenerator;
    private final TokenIDGenerator      tokenIDGenerator;

    public SessionServiceImpl(
        SessionRepository sessionRepository,
        JwtUtil jwtUtil,
        SessionKeyGenerator sessionKeyGenerator,
        TokenIDGenerator tokenIDGenerator
    ) {
        this.sessionRepository = sessionRepository;
        this.jwtUtil = jwtUtil;
        this.sessionKeyGenerator = sessionKeyGenerator;
        this.tokenIDGenerator = tokenIDGenerator;
    }

    @Override
    public Session createSession(String userID, String[] roles) {
        String sessionPostfix = sessionKeyGenerator.generate();
        String sessionID = generateSessionID(userID,sessionPostfix);
        String tokenID = tokenIDGenerator.generate();

        return sessionRepository.insert(new Session(
            userID,
            sessionPostfix,
            Instant.now(),
            roles,
            generateAccessToken(tokenID, userID, sessionID, roles),
            generateRefreshToken(tokenID, userID, sessionID, roles),
            tokenID
        ));
    }

    @Override
    public Session getSession(String sessionID) {
        SessionID key = parseSessionID(sessionID).orElseThrow(() -> new SessionNotFoundException(sessionID));

        return sessionRepository
            .findByID(key)
            .orElseThrow(() -> new SessionNotFoundException(sessionID));
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
            Claims claims = jwtUtil.getRefreshTokenParser().parseClaimsJws(refreshToken).getBody();
            String sessionID = claims.get("sid", String.class);
            SessionID key = parseSessionID(sessionID).orElseThrow(InvalidTokenException::new);

            Session session = sessionRepository
                .findByID(key)
                .orElseThrow(InvalidTokenException::new);

            if ( ! claims.getId().equals(session.getTokenID())) throw new InvalidTokenException();

            String tokenID = tokenIDGenerator.generate();

            session.setAccessToken(generateAccessToken(
                tokenID,
                session.getUserID(),
                sessionID,
                session.getRoles()
            ));

            session.setRefreshToken(generateRefreshToken(
                tokenID,
                session.getUserID(),
                sessionID,
                session.getRoles()
            ));

            session.setTokenID(tokenID);
            session.setCreatedAt(Instant.now());

            sessionRepository.deleteByKey(session.getUserID(), session.getSessionKey());

            return sessionRepository.insert(session);
        } catch (
            SignatureException |
            MalformedJwtException |
            ExpiredJwtException |
            UnsupportedJwtException |
            IllegalArgumentException e
        ) {
            throw new InvalidTokenException();
        }
    }

    @Override
    public void deleteSession(String refreshToken) {
        try {
            Claims claims = jwtUtil.getRefreshTokenParser().parseClaimsJws(refreshToken).getBody();
            String sessionID = claims.get("sid", String.class);
            SessionID key = parseSessionID(sessionID).orElseThrow(InvalidTokenException::new);

            Session session = sessionRepository
                .findByID(key)
                .orElseThrow(InvalidTokenException::new);

            if ( ! claims.getId().equals(session.getTokenID())) throw new InvalidTokenException();

            if ( ! sessionRepository.deleteByKey(key.getUserID(), key.getSessionKey())) {
                throw new SessionRemovingException(sessionID);
            }
        } catch (
            SignatureException |
            MalformedJwtException |
            ExpiredJwtException |
            UnsupportedJwtException |
            IllegalArgumentException e
        ) {
            throw new InvalidTokenException();
        }
    }

    @Override
    public void deleteSessionsByUserID(String userID) {
        if (sessionRepository.existsByUserID(userID)) {
            if ( ! sessionRepository.deleteAllByUserID(userID)) {
                throw new UserSessionsRemovingException(userID);
            }
        } else {
            throw new UserSessionsNotFoundException(userID);
        }
    }

    private String generateSessionID(String userID, String sessionPostfix) {
        return new StringBuilder(userID).append(".").append(sessionPostfix).toString();
    }

    private Optional<SessionID> parseSessionID(String sessionID) {
        if (sessionID.length() < sessionKeyGenerator.getKeyLength() + 2) return Optional.empty();

        StringBuilder builder = new StringBuilder(sessionID);
        int separatorIndex = builder.length() - 1 - sessionKeyGenerator.getKeyLength();

        return Optional.of(new SessionID(
            builder.substring(0,separatorIndex),
            builder.substring(separatorIndex + 1)
        ));
    }

    private String generateRefreshToken(String tokenID, String userID, String sessionID, String[] roles) {
        return jwtUtil.generateRefreshTokenBuilder()
            .setId(tokenID)
            .setSubject(userID)
            .claim("roles", roles)
            .claim("sid", sessionID)
            .compact();
    }

    private String generateAccessToken(String tokenID, String userID, String sessionID, String[] roles) {
        return jwtUtil.generateAccessTokenBuilder()
            .setId(tokenID)
            .setSubject(userID)
            .claim("roles", roles)
            .claim("sid", sessionID)
            .compact();
    }
}

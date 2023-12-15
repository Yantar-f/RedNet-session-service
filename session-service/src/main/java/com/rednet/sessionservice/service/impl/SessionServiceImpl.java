package com.rednet.sessionservice.service.impl;

import com.rednet.sessionservice.entity.Session;
import com.rednet.sessionservice.entity.SessionKey;
import com.rednet.sessionservice.exception.impl.InvalidTokenException;
import com.rednet.sessionservice.exception.impl.SessionNotFoundException;
import com.rednet.sessionservice.exception.impl.SessionRemovingException;
import com.rednet.sessionservice.exception.impl.UserSessionsNotFound;
import com.rednet.sessionservice.exception.impl.UserSessionsRemovingException;
import com.rednet.sessionservice.repository.SessionRepository;
import com.rednet.sessionservice.service.SessionService;
import com.rednet.sessionservice.util.JwtUtil;
import com.rednet.sessionservice.util.SessionPostfixGenerator;
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
    private final SessionRepository sessionRepository;
    private final JwtUtil jwtUtil;
    private final SessionPostfixGenerator sessionPostfixGenerator;
    private final TokenIDGenerator tokenIDGenerator;

    public SessionServiceImpl(
        SessionRepository sessionRepository,
        JwtUtil jwtUtil,
        SessionPostfixGenerator sessionPostfixGenerator,
        TokenIDGenerator tokenIDGenerator
    ) {
        this.sessionRepository = sessionRepository;
        this.jwtUtil = jwtUtil;
        this.sessionPostfixGenerator = sessionPostfixGenerator;
        this.tokenIDGenerator = tokenIDGenerator;
    }

    @Override
    public Session createSession(String userID, String[] roles) {
        String sessionPostfix = sessionPostfixGenerator.generate();
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
        SessionKey key = parseSessionID(sessionID).orElseThrow(() -> new SessionNotFoundException(sessionID));

        return sessionRepository
            .findByID(key.getUserID(), key.getSessionPostfix())
            .orElseThrow(() -> new SessionNotFoundException(sessionID));
    }

    @Override
    public List<Session> getSessionsByUserID(String userID) {
        List<Session> sessions = sessionRepository.findAllByUserID(userID);

        if (sessions.isEmpty()) throw new UserSessionsNotFound(userID);

        return sessions;
    }

    @Override
    public Session refreshSession(String refreshToken) {
        try {
            Claims claims = jwtUtil.getRefreshTokenParser().parseClaimsJws(refreshToken).getBody();
            String sessionID = claims.get("sid", String.class);
            SessionKey key = parseSessionID(sessionID).orElseThrow(InvalidTokenException::new);

            Session session = sessionRepository
                .findByID(key.getUserID(), key.getSessionPostfix())
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

            sessionRepository.deleteByID(session.getUserID(), session.getSessionPostfix());

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
            SessionKey key = parseSessionID(sessionID).orElseThrow(InvalidTokenException::new);

            Session session = sessionRepository
                .findByID(key.getUserID(), key.getSessionPostfix())
                .orElseThrow(InvalidTokenException::new);

            if ( ! claims.getId().equals(session.getTokenID())) throw new InvalidTokenException();

            if ( ! sessionRepository.deleteByID(key.getUserID(), key.getSessionPostfix())) {
                throw new SessionRemovingException(sessionID);
            }
        }
        catch (
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
            throw new UserSessionsNotFound(userID);
        }
    }

    private String generateSessionID(String userID, String sessionPostfix) {
        return new StringBuilder(userID).append(".").append(sessionPostfix).toString();
    }

    private Optional<SessionKey> parseSessionID(String sessionID) {
        if (sessionID.length() < sessionPostfixGenerator.getPostfixLength() + 2) return Optional.empty();

        StringBuilder builder = new StringBuilder(sessionID);
        int separatorIndex = builder.length() - 1 - sessionPostfixGenerator.getPostfixLength();

        return Optional.of(new SessionKey(
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

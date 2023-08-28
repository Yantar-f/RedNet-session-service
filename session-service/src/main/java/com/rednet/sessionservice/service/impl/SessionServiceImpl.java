package com.rednet.sessionservice.service.impl;

import com.rednet.sessionservice.entity.Session;
import com.rednet.sessionservice.entity.SessionKey;
import com.rednet.sessionservice.exception.impl.InvalidTokenException;
import com.rednet.sessionservice.exception.impl.SessionNotFoundException;
import com.rednet.sessionservice.exception.impl.UserSessionsNotFound;
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

import java.util.List;

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

        return sessionRepository.save(new Session(
            new SessionKey(userID, sessionPostfix),
            roles,
            generateAccessToken(tokenID, userID, sessionID, roles),
            generateRefreshToken(tokenID, userID, sessionID, roles),
            tokenID
        ));
    }

    @Override
    public Session getSession(String sessionID) {
        SessionKey key = parseSessionID(sessionID);
        return sessionRepository.findById(key).orElseThrow(() -> new SessionNotFoundException(sessionID));
    }

    @Override
    public List<Session> getSessionsByUserID(String userID) {
        List<Session> sessions = sessionRepository.findAllBySessionKey_UserID(userID);

        if (sessions.isEmpty()) throw new UserSessionsNotFound(userID);

        return sessions;
    }

    @Override
    public Session refreshSession(String refreshToken) {
        try {
            Claims claims = jwtUtil.getRefreshTokenParser().parseClaimsJws(refreshToken).getBody();
            String sessionID = claims.get("sid", String.class);
            SessionKey key = parseSessionID(sessionID);

            Session session = sessionRepository.findById(key).orElseThrow(InvalidTokenException::new);

            if ( ! claims.getId().equals(session.getTokenID())) throw new InvalidTokenException();

            String tokenID = tokenIDGenerator.generate();

            session.setAccessToken(generateAccessToken(
                tokenID,
                session.getSessionKey().getUserID(),
                sessionID,
                session.getRoles()
            ));

            session.setRefreshToken(generateRefreshToken(
                tokenID,
                session.getSessionKey().getUserID(),
                sessionID,
                session.getRoles()
            ));

            session.setTokenID(tokenID);

            return sessionRepository.save(session);
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
            SessionKey key = parseSessionID(sessionID);

            Session session = sessionRepository
                .findById(key)
                .orElseThrow(InvalidTokenException::new);

            if ( ! claims.getId().equals(session.getTokenID())) throw new InvalidTokenException();

            sessionRepository.deleteById(key);
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
        if (sessionRepository.existsAllBySessionKey_UserID(userID)) {
            sessionRepository.deleteAllBySessionKey_UserID(userID);
        } else {
            throw new UserSessionsNotFound(userID);
        }
    }

    private String generateSessionID(String userID, String sessionPostfix) {
        return new StringBuilder(userID).append(".").append(sessionPostfix).toString();
    }

    private SessionKey parseSessionID(String sessionID) {
        if (sessionID.length() < sessionPostfixGenerator.getPostfixLength() + 2) throw new InvalidTokenException();

        StringBuilder builder = new StringBuilder(sessionID);
        int separatorIndex = builder.length() - 1 - sessionPostfixGenerator.getPostfixLength();

        return new SessionKey(
            builder.substring(0,separatorIndex),
            builder.substring(separatorIndex + 1)
        );
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

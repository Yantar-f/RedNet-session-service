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

    public SessionServiceImpl(
        SessionRepository sessionRepository,
        JwtUtil jwtUtil,
        SessionPostfixGenerator sessionPostfixGenerator
    ) {
        this.sessionRepository = sessionRepository;
        this.jwtUtil = jwtUtil;
        this.sessionPostfixGenerator = sessionPostfixGenerator;
    }

    @Override
    public Session createSession(String userID, String[] roles) {
        String sessionPostfix = sessionPostfixGenerator.generate();
        String sessionID = generateSessionID(userID,sessionPostfix);

        return sessionRepository.save(new Session(
            new SessionKey(userID, sessionPostfix),
            roles,
            generateAccessToken(userID,sessionID, roles),
            generateRefreshToken(userID,sessionID, roles)
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

            Session session = sessionRepository
                .findById(key)
                .orElseThrow(() -> new SessionNotFoundException(sessionID));

            if ( ! refreshToken.equals(session.getRefreshToken())) throw new InvalidTokenException();

            session.setAccessToken(jwtUtil.generateAccessTokenBuilder()
                .setSubject(session.getSessionKey().getUserID())
                .claim("sid", sessionID)
                .claim("roles", session.getRoles())
                .compact());

            session.setRefreshToken(jwtUtil.generateRefreshTokenBuilder()
                .setSubject(session.getSessionKey().getUserID())
                .claim("sid", sessionID)
                .claim("roles", session.getRoles())
                .compact());

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
            SessionKey key = parseSessionID(claims.get("sid", String.class));
            sessionRepository.deleteById(key);
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
        sessionRepository.deleteAllBySessionKey_UserID(userID);
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

    private String generateRefreshToken(String userID, String sessionID, String[] roles) {
        return jwtUtil.generateRefreshTokenBuilder()
            .setSubject(userID)
            .claim("roles", roles)
            .claim("sid", sessionID)
            .compact();
    }

    private String generateAccessToken(String userID, String sessionID, String[] roles) {
        return jwtUtil.generateAccessTokenBuilder()
            .setSubject(userID)
            .claim("roles", roles)
            .claim("sid", sessionID)
            .compact();
    }
}

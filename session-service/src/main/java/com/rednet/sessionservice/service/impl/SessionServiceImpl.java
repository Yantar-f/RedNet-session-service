package com.rednet.sessionservice.service.impl;

import com.rednet.sessionservice.entity.Session;
import com.rednet.sessionservice.entity.SessionKey;
import com.rednet.sessionservice.exception.impl.InvalidTokenException;
import com.rednet.sessionservice.exception.impl.SessionNotFoundException;
import com.rednet.sessionservice.exception.impl.UserSessionsNotFound;
import com.rednet.sessionservice.payload.request.CreateSessionRequestBody;
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
    public Session createSession(CreateSessionRequestBody requestBody) {
        String sessionPostfix = sessionPostfixGenerator.generate();
        String sessionID = generateSessionID(requestBody.userID(),sessionPostfix);

        return sessionRepository.save(new Session(
            new SessionKey(requestBody.userID(), sessionPostfix),
            requestBody.roles(),
            generateAccessToken(requestBody.userID(),sessionID, requestBody.roles()),
            generateRefreshToken(requestBody.userID(),sessionID, requestBody.roles())
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
            String sessionID = (String)claims.get("sid");
            String[] roles = (String[]) claims.get("roles");
            SessionKey key = parseSessionID(sessionID);
            Session session = new Session(
                key,
                roles,
                generateAccessToken(key.getUserID(), sessionID, roles),
                generateRefreshToken(key.getUserID(),sessionID, roles)
            );

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
    public boolean deleteSession(String refreshToken) {
        try {
            Claims claims = jwtUtil.getRefreshTokenParser().parseClaimsJws(refreshToken).getBody();
            SessionKey key = parseSessionID((String)claims.get("sid"));
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

        return true;
    }

    @Override
    public boolean deleteSessionsByUserID(String userID) {
        sessionRepository.deleteAllBySessionKey_UserID(userID);
        return true;
    }


    private String generateSessionID(String userID, String sessionPostfix) {
        return new StringBuilder(userID).append(".").append(sessionPostfix).toString();
    }

    private SessionKey parseSessionID(String sessionID) {
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

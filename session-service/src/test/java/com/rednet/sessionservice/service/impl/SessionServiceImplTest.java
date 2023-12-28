package com.rednet.sessionservice.service.impl;

import com.rednet.sessionservice.entity.Session;
import com.rednet.sessionservice.exception.impl.InvalidTokenException;
import com.rednet.sessionservice.exception.impl.SessionNotFoundException;
import com.rednet.sessionservice.exception.impl.SessionRemovingException;
import com.rednet.sessionservice.exception.impl.UserSessionsNotFoundException;
import com.rednet.sessionservice.exception.impl.UserSessionsRemovingException;
import com.rednet.sessionservice.model.SessionID;
import com.rednet.sessionservice.repository.SessionRepository;
import com.rednet.sessionservice.util.JwtUtil;
import com.rednet.sessionservice.util.SessionKeyGenerator;
import com.rednet.sessionservice.util.TokenIDGenerator;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static io.jsonwebtoken.io.Decoders.BASE64;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


class SessionServiceImplTest {
    int         sessionPostfixLength = 4;
    String      expectedUserID = "user";
    String      expectedSessionPostfix = "1234";
    String      expectedTokenID = "4321";
    String      expectedSessionID = expectedUserID + '.' + expectedSessionPostfix;
    String      expectedAccessToken = "a-token";
    String      expectedRefreshToken = "r-token";
    String      accessTokenSecretKey = "suF25ZudSgyzQSS9QgpaSyUt5XZZGtTayc22RlLe5IX1erTOz64mN5BarqeiPV2s";
    String      refreshTokenSecretKey = "a1yTJjPn3+N8p7y3bANWFg+mOpQH6WWrSfKq2NM4f9YFNsKK8U4VRx6Godo3OeEf";
    String[]    expectedRoles = new String[]{"role"};
    Instant     expectedCreatedAt = Instant.now();

    JwtParser accessTokenParser = Jwts.parserBuilder()
        .setSigningKey(Keys.hmacShaKeyFor(BASE64.decode(accessTokenSecretKey)))
        .build();

    JwtParser refreshTokenParser = Jwts.parserBuilder()
        .setSigningKey(Keys.hmacShaKeyFor(BASE64.decode(refreshTokenSecretKey)))
        .build();

    SessionRepository       sessionRepository = mock(SessionRepository.class);
    JwtUtil                 jwtUtil = mock(JwtUtil.class);
    SessionKeyGenerator sessionKeyGenerator = mock(SessionKeyGenerator.class);
    TokenIDGenerator        tokenIDGenerator = mock(TokenIDGenerator.class);

    SessionServiceImpl sut = new SessionServiceImpl(
        sessionRepository,
        jwtUtil,
            sessionKeyGenerator,
        tokenIDGenerator
    );

    @Test
    void createSession() {
        when(jwtUtil.generateAccessTokenBuilder()).thenReturn(generateTestAccessTokenBuilder());
        when(jwtUtil.generateRefreshTokenBuilder()).thenReturn(generateTestRefreshTokenBuilder());
        when(sessionKeyGenerator.generate()).thenReturn(expectedSessionPostfix);
        when(tokenIDGenerator.generate()).thenReturn(expectedTokenID);
        when(sessionRepository.insert(any(Session.class))).then(returnsFirstArg());

        Session actualSession = sut.createSession(expectedUserID, expectedRoles);

        assertEquals(expectedUserID, actualSession.getUserID());
        assertEquals(expectedSessionPostfix, actualSession.getSessionKey());
        assertEquals(expectedRoles.length, actualSession.getRoles().length);
        assertEquals(expectedTokenID, actualSession.getTokenID());
        assertTrue(compareStringArraysContent(expectedRoles, actualSession.getRoles()));

        assertDoesNotThrow(() -> {
            Claims claims = accessTokenParser.parseClaimsJws(actualSession.getAccessToken()).getBody();
            List<String> actualTokenRoles = claims.get("roles", ArrayList.class);

            assertEquals("access", claims.get("test"));
            assertEquals(expectedSessionID, claims.get("sid"));
            assertEquals(expectedUserID, claims.getSubject());
            assertEquals(expectedTokenID, claims.getId());
            assertEquals(expectedRoles.length, actualTokenRoles.size());
            assertTrue(compareStringArraysContent(expectedRoles, actualTokenRoles.toArray(String[]::new)));
        });

        assertDoesNotThrow(() -> {
            Claims claims = refreshTokenParser.parseClaimsJws(actualSession.getRefreshToken()).getBody();
            List<String> actualTokenRoles = claims.get("roles", ArrayList.class);

            assertEquals("refresh", claims.get("test"));
            assertEquals(expectedSessionID, claims.get("sid"));
            assertEquals(expectedUserID, claims.getSubject());
            assertEquals(expectedTokenID, claims.getId());
            assertEquals(expectedRoles.length, actualTokenRoles.size());
            assertTrue(compareStringArraysContent(expectedRoles, actualTokenRoles.toArray(String[]::new)));
        });

        verify(jwtUtil).generateAccessTokenBuilder();
        verify(jwtUtil).generateRefreshTokenBuilder();
        verify(sessionKeyGenerator).generate();
        verify(tokenIDGenerator).generate();

        verify(sessionRepository).insert(argThat(session ->
            session.getUserID().equals(expectedUserID) &&
            session.getSessionKey().equals(expectedSessionPostfix) &&
            compareStringArraysContent(expectedRoles, session.getRoles())
        ));
    }

    @Test
    void getSession() {
        Session expectedSession = new Session(
            expectedUserID,
                expectedSessionPostfix,
            expectedCreatedAt,
            expectedRoles,
            "a-token",
            "r-token",
            expectedTokenID
        );

        SessionID expectedSessionIDModel = new SessionID(expectedUserID, expectedSessionPostfix);

        when(sessionKeyGenerator.getKeyLength()).thenReturn(sessionPostfixLength);
        when(sessionRepository.findByID(any())).thenReturn(Optional.of(expectedSession));

        Session actualSession = sut.getSession(expectedSessionID);

        assertEquals(expectedSession, actualSession);

        verify(sessionKeyGenerator, atLeastOnce()).getKeyLength();
        verify(sessionRepository).findByID(eq(expectedSessionIDModel));
    }

    @Test
    void getSession_SessionNotFound() {
        when(sessionKeyGenerator.getKeyLength()).thenReturn(sessionPostfixLength);
        when(sessionRepository.findByID(any())).thenReturn(Optional.empty());

        assertThrows(SessionNotFoundException.class, () -> sut.getSession(expectedSessionID));

        verify(sessionKeyGenerator, atLeastOnce()).getKeyLength();
        verify(sessionRepository).findByID(any());
    }

    @Test
    void getSessionsByUserID() {
        Session
            session1 = new Session(
                expectedUserID,
                "1111",
                Instant.now(),
                new String[]{"role1", "role2"},
                "a-token1",
                "r-token1",
                "token-id1"
            ),

            session2 = new Session(
                expectedUserID,
                "1112",
                Instant.now(),
                new String[]{"role"},
                "a-token2",
                "r-token2",
                "token-id1"
            );

        List<Session> expectedUserSessions = List.of(session1, session2);

        when(sessionRepository.findAllByUserID(any())).thenReturn(expectedUserSessions);

        assertDoesNotThrow(() -> {
            List<Session> actualUserSessions = sut.getSessionsByUserID(expectedUserID);
            assertEquals(expectedUserSessions.size(), actualUserSessions.size());

            expectedUserSessions.forEach(expectedSession ->
                assertTrue(actualUserSessions.stream().anyMatch(actualSession ->
                    compare(expectedSession, actualSession)
                ))
            );
        });

        verify(sessionRepository).findAllByUserID(eq(expectedUserID));
    }

    @Test
    void getSessionsByUserID_UserSessionsNotFound() {
        List<Session> expectedUserSessions = List.of();

        when(sessionRepository.findAllByUserID(any())).thenReturn(expectedUserSessions);

        assertThrows(UserSessionsNotFoundException.class, () -> sut.getSessionsByUserID(expectedUserID));

        verify(sessionRepository).findAllByUserID(eq(expectedUserID));
    }

    @Test
    void refreshSession() {
        String oldTokenID = "1221";

        String accessToken = generateTestAccessTokenBuilder()
            .setId(oldTokenID)
            .setSubject(expectedUserID)
            .claim("roles", expectedRoles)
            .claim("sid", expectedSessionID)
            .compact();

        String refreshToken = generateTestRefreshTokenBuilder()
            .setId(oldTokenID)
            .setSubject(expectedUserID)
            .claim("roles", expectedRoles)
            .claim("sid", expectedSessionID)
            .compact();

        Session session = new Session(
            expectedUserID,
                expectedSessionPostfix,
                expectedCreatedAt,
            expectedRoles,
            accessToken,
            refreshToken,
            oldTokenID
        );

        SessionID sessionIDModel = new SessionID(expectedUserID, expectedSessionPostfix);

        when(jwtUtil.getRefreshTokenParser()).thenReturn(refreshTokenParser);
        when(jwtUtil.generateAccessTokenBuilder()).thenReturn(generateTestAccessTokenBuilder());
        when(jwtUtil.generateRefreshTokenBuilder()).thenReturn(generateTestRefreshTokenBuilder());
        when(sessionKeyGenerator.getKeyLength()).thenReturn(sessionPostfixLength);
        when(tokenIDGenerator.generate()).thenReturn(expectedTokenID);
        when(sessionRepository.findByID(any())).thenReturn(Optional.of(session));
        when(sessionRepository.insert(any(Session.class))).then(returnsFirstArg());
        when(sessionRepository.deleteByKey(any(),any())).thenReturn(true);

        assertDoesNotThrow(() -> {
            Session newSession = sut.refreshSession(refreshToken);

            assertEquals(expectedUserID, newSession.getUserID());
            assertEquals(expectedSessionPostfix, newSession.getSessionKey());
            assertEquals(expectedTokenID, newSession.getTokenID());
            assertTrue(expectedCreatedAt.isBefore(newSession.getCreatedAt()));
            assertTrue(compareStringArraysContent(expectedRoles, newSession.getRoles()));

            assertDoesNotThrow(() -> {
                Claims claims = accessTokenParser.parseClaimsJws(newSession.getAccessToken()).getBody();
                List<String> actualTokenRoles = claims.get("roles", ArrayList.class);

                assertEquals("access", claims.get("test"));
                assertEquals(expectedSessionID, claims.get("sid"));
                assertEquals(expectedUserID, claims.getSubject());
                assertEquals(expectedTokenID, claims.getId());
                assertTrue(compareStringArraysContent(expectedRoles, actualTokenRoles.toArray(String[]::new)));
            });

            assertDoesNotThrow(() -> {
                Claims claims = refreshTokenParser.parseClaimsJws(newSession.getRefreshToken()).getBody();
                List<String> actualTokenRoles = claims.get("roles", ArrayList.class);

                assertEquals("refresh", claims.get("test"));
                assertEquals(expectedSessionID, claims.get("sid"));
                assertEquals(expectedUserID, claims.getSubject());
                assertEquals(expectedTokenID, claims.getId());
                assertTrue(compareStringArraysContent(expectedRoles, actualTokenRoles.toArray(String[]::new)));
            });
        });

        verify(jwtUtil).getRefreshTokenParser();
        verify(jwtUtil).generateAccessTokenBuilder();
        verify(jwtUtil).generateRefreshTokenBuilder();
        verify(sessionKeyGenerator, atLeastOnce()).getKeyLength();
        verify(tokenIDGenerator).generate();
        verify(sessionRepository).deleteByKey(eq(expectedUserID), eq(expectedSessionPostfix));
        verify(sessionRepository).findByID(eq(sessionIDModel));
    }

    @Test
    void refreshSession_InvalidToken() {
        String invalidToken = "head.payload.signature";

        when(jwtUtil.getRefreshTokenParser()).thenReturn(refreshTokenParser);

        assertThrows(InvalidTokenException.class, () -> sut.refreshSession(invalidToken));

        verify(jwtUtil).getRefreshTokenParser();
        verify(sessionKeyGenerator, never()).getKeyLength();
        verify(sessionRepository, never()).findByID(any());
        verify(jwtUtil, never()).generateAccessTokenBuilder();
        verify(jwtUtil, never()).generateRefreshTokenBuilder();
        verify(tokenIDGenerator, never()).generate();
        verify(sessionRepository, never()).deleteByKey(any(), any());
    }

    @Test
    void refreshSession_InvalidToken_InvalidSessionID() {
        String invalidToken = generateTestRefreshTokenBuilder()
            .setSubject(expectedUserID)
            .claim("roles", expectedRoles)
            .claim("sid", expectedUserID + '.' + "1233")
            .compact();

        when(jwtUtil.getRefreshTokenParser()).thenReturn(refreshTokenParser);
        when(sessionKeyGenerator.getKeyLength()).thenReturn(sessionPostfixLength);
        when(sessionRepository.findByID(any())).thenReturn(Optional.empty());

        assertThrows(InvalidTokenException.class, () -> sut.refreshSession(invalidToken));

        verify(jwtUtil).getRefreshTokenParser();
        verify(sessionKeyGenerator, atLeastOnce()).getKeyLength();
        verify(sessionRepository).findByID(any());
        verify(jwtUtil, never()).generateAccessTokenBuilder();
        verify(jwtUtil, never()).generateRefreshTokenBuilder();
        verify(tokenIDGenerator, never()).generate();
        verify(sessionRepository, never()).deleteByKey(any(), any());
    }

    @Test
    void refreshSession_InvalidToken_UsedToken() {
        String invalidToken = Jwts.builder()
            .signWith(Keys.hmacShaKeyFor(BASE64.decode(refreshTokenSecretKey)))
            .setId("id2")
            .setSubject(expectedUserID)
            .claim("roles", expectedRoles)
            .claim("sid", expectedSessionID)
            .compact();

        Session expectedSession = new Session(
            expectedUserID,
                expectedSessionPostfix,
            expectedCreatedAt,
            expectedRoles,
            "a-token",
            "r-token",
            "id1"
        );

        when(jwtUtil.getRefreshTokenParser()).thenReturn(refreshTokenParser);
        when(sessionKeyGenerator.getKeyLength()).thenReturn(sessionPostfixLength);
        when(sessionRepository.findByID(any())).thenReturn(Optional.of(expectedSession));

        assertThrows(InvalidTokenException.class, () -> sut.refreshSession(invalidToken));

        verify(jwtUtil).getRefreshTokenParser();
        verify(sessionKeyGenerator, atLeastOnce()).getKeyLength();
        verify(sessionRepository).findByID(any());
        verify(jwtUtil, never()).generateAccessTokenBuilder();
        verify(jwtUtil, never()).generateRefreshTokenBuilder();
        verify(tokenIDGenerator, never()).generate();
        verify(sessionRepository, never()).deleteByKey(any(), any());
    }

    @Test
    void deleteSession() {
        String token = generateTestRefreshTokenBuilder()
            .claim("sid", expectedSessionID)
            .setId(expectedTokenID)
            .compact();

        Session session = new Session(
            expectedUserID,
                expectedSessionPostfix,
            expectedCreatedAt,
            expectedRoles,
            "a-token",
            token,
            expectedTokenID
        );

        when(sessionKeyGenerator.getKeyLength()).thenReturn(sessionPostfixLength);
        when(jwtUtil.getRefreshTokenParser()).thenReturn(refreshTokenParser);
        when(sessionRepository.findByID(any())).thenReturn(Optional.of(session));
        when(sessionRepository.deleteByKey(any(), any())).thenReturn(true);

        assertDoesNotThrow(() -> sut.deleteSession(token));

        verify(jwtUtil).getRefreshTokenParser();
        verify(sessionKeyGenerator, atLeastOnce()).getKeyLength();
        verify(sessionRepository).findByID(any());
        verify(sessionRepository).deleteByKey(eq(expectedUserID), eq(expectedSessionPostfix));
    }

    @Test
    void deleteSession_SessionRemovingError() {
        String token = generateTestRefreshTokenBuilder()
            .claim("sid", expectedSessionID)
            .setId(expectedTokenID)
            .compact();

        Session session = new Session(
            expectedUserID,
                expectedSessionPostfix,
            expectedCreatedAt,
            expectedRoles,
            "a-token",
            token,
            expectedTokenID
        );

        when(sessionKeyGenerator.getKeyLength()).thenReturn(sessionPostfixLength);
        when(jwtUtil.getRefreshTokenParser()).thenReturn(refreshTokenParser);
        when(sessionRepository.findByID(any())).thenReturn(Optional.of(session));
        when(sessionRepository.deleteByKey(any(), any())).thenReturn(false);

        assertThrows(SessionRemovingException.class,() -> sut.deleteSession(token));

        verify(jwtUtil).getRefreshTokenParser();
        verify(sessionKeyGenerator, atLeastOnce()).getKeyLength();
        verify(sessionRepository).findByID(any());
        verify(sessionRepository).deleteByKey(eq(expectedUserID), eq(expectedSessionPostfix));
    }

    @Test
    void deleteSession_InvalidToken() {
        String invalidRefreshToken = "r-token";

        Session session = new Session(
            expectedUserID,
                expectedSessionPostfix,
            expectedCreatedAt,
            expectedRoles,
            "a-token",
            "4token",
            expectedTokenID
        );

        when(sessionKeyGenerator.getKeyLength()).thenReturn(sessionPostfixLength);
        when(jwtUtil.getRefreshTokenParser()).thenReturn(refreshTokenParser);
        when(sessionRepository.findByID(any())).thenReturn(Optional.of(session));

        assertThrows(InvalidTokenException.class, () -> sut.deleteSession(invalidRefreshToken));

        verify(jwtUtil).getRefreshTokenParser();
        verify(sessionKeyGenerator, never()).getKeyLength();
        verify(sessionRepository, never()).findByID(any());
        verify(sessionRepository, never()).deleteByKey(any(), any());
    }

    @Test
    void deleteSession_InvalidToken_UsedToken() {
        String invalidRefreshToken = generateTestRefreshTokenBuilder()
            .claim("sid", expectedSessionID)
            .setId("id")
            .compact();

        Session session = new Session(
            expectedUserID,
                expectedSessionPostfix,
            expectedCreatedAt,
            expectedRoles,
            "a-token",
            generateTestRefreshTokenBuilder()
                .claim("sid", expectedSessionID)
                .setId(expectedTokenID)
                .compact(),
            expectedTokenID
        );

        when(sessionKeyGenerator.getKeyLength()).thenReturn(sessionPostfixLength);
        when(jwtUtil.getRefreshTokenParser()).thenReturn(refreshTokenParser);
        when(sessionRepository.findByID(any())).thenReturn(Optional.of(session));

        assertThrows(InvalidTokenException.class, () -> sut.deleteSession(invalidRefreshToken));

        verify(jwtUtil).getRefreshTokenParser();
        verify(sessionKeyGenerator, atLeastOnce()).getKeyLength();

        verify(sessionRepository).findByID(any());
        verify(sessionRepository, never()).deleteByKey(any(), any());
    }

    @Test
    void deleteSession_InvalidToken_InvalidSessionID() {
        String invalidRefreshToken = generateTestRefreshTokenBuilder()
            .claim("sid", "invalidSessionID")
            .setId("id")
            .compact();

        Session session = new Session(
            expectedUserID,
                expectedSessionPostfix,
            expectedCreatedAt,
            expectedRoles,
            "a-token",
            generateTestRefreshTokenBuilder()
                .claim("sid", expectedSessionID)
                .setId(expectedTokenID)
                .compact(),
            expectedTokenID
        );

        when(sessionKeyGenerator.getKeyLength()).thenReturn(sessionPostfixLength);
        when(jwtUtil.getRefreshTokenParser()).thenReturn(refreshTokenParser);
        when(sessionRepository.findByID(any())).thenReturn(Optional.empty());

        assertThrows(InvalidTokenException.class, () -> sut.deleteSession(invalidRefreshToken));

        verify(jwtUtil).getRefreshTokenParser();
        verify(sessionKeyGenerator, atLeastOnce()).getKeyLength();
        verify(sessionRepository).findByID(any());
        verify(sessionRepository, never()).deleteByKey(any(), any());
    }

    @Test
    void deleteSessionsByUserID() {
        when(sessionRepository.existsByUserID(any())).thenReturn(true);
        when(sessionRepository.deleteAllByUserID(any())).thenReturn(true);

        assertDoesNotThrow(() -> sut.deleteSessionsByUserID(expectedUserID));

        verify(sessionRepository).existsByUserID(eq(expectedUserID));
        verify(sessionRepository).deleteAllByUserID(eq(expectedUserID));
    }

    @Test
    void deleteSessionsByUserID_UserSessionsNotFound() {
        when(sessionRepository.existsByUserID(any())).thenReturn(false);

        assertThrows(UserSessionsNotFoundException.class, () -> sut.deleteSessionsByUserID(expectedUserID));

        verify(sessionRepository).existsByUserID(eq(expectedUserID));
        verify(sessionRepository, never()).deleteAllByUserID(any());
    }

    @Test
    void deleteSessionsByUserID_UserSessionsRemovingError() {
        when(sessionRepository.existsByUserID(any())).thenReturn(true);
        when(sessionRepository.deleteAllByUserID(any())).thenReturn(false);

        assertThrows(UserSessionsRemovingException.class, () -> sut.deleteSessionsByUserID(expectedUserID));

        verify(sessionRepository).existsByUserID(eq(expectedUserID));
        verify(sessionRepository).deleteAllByUserID(eq(expectedUserID));
    }

    private static boolean compare(Session session1, Session session2) {
        if (
            session1.getUserID().equals(session2.getUserID()) &&
            session1.getSessionKey().equals(session2.getSessionKey()) &&
            session1.getCreatedAt().equals(session2.getCreatedAt()) &&
            session1.getRoles().length == session2.getRoles().length &&
            session1.getAccessToken().equals(session2.getAccessToken()) &&
            session1.getRefreshToken().equals(session2.getRefreshToken()) &&
            session1.getTokenID().equals(session2.getTokenID())
        ) {
            for (String role : session1.getRoles()) {
                if (!Arrays.asList(session2.getRoles()).contains(role)) return false;
            }

            return true;
        }

        return false;
    }

    private boolean compareStringArraysContent(String[] expectedArray, String[] actualArray) {
        if (expectedArray.length != actualArray.length) return false;

        for (String expectedStr : expectedArray) if ( ! Arrays.asList(actualArray).contains(expectedStr)) return false;

        return true;
    }

    private JwtBuilder generateTestAccessTokenBuilder() {
        return Jwts.builder()
            .signWith(Keys.hmacShaKeyFor(BASE64.decode(accessTokenSecretKey)))
            .claim("test", "access");
    }

    private JwtBuilder generateTestRefreshTokenBuilder() {
        return Jwts.builder()
            .signWith(Keys.hmacShaKeyFor(BASE64.decode(refreshTokenSecretKey)))
            .claim("test", "refresh");
    }
}
package com.rednet.sessionservice.service.impl;

import com.rednet.sessionservice.entity.Session;
import com.rednet.sessionservice.entity.SessionKey;
import com.rednet.sessionservice.exception.impl.InvalidTokenException;
import com.rednet.sessionservice.exception.impl.SessionNotFoundException;
import com.rednet.sessionservice.exception.impl.UserSessionsNotFound;
import com.rednet.sessionservice.repository.SessionRepository;
import com.rednet.sessionservice.util.JwtUtil;
import com.rednet.sessionservice.util.SessionPostfixGenerator;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

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
    int sessionPostfixLength = 4;
    String expectedUserID = "user";
    String sessionPostfix = "1234";
    String expectedSessionID = expectedUserID + '.' + sessionPostfix;
    String[] expectedRoles = new String[]{"role"};
    String accessTokenSecretKey = "suF25ZudSgyzQSS9QgpaSyUt5XZZGtTayc22RlLe5IX1erTOz64mN5BarqeiPV2s";
    String refreshTokenSecretKey = "a1yTJjPn3+N8p7y3bANWFg+mOpQH6WWrSfKq2NM4f9YFNsKK8U4VRx6Godo3OeEf";

    JwtParser accessTokenParser = Jwts.parserBuilder()
        .setSigningKey(Keys.hmacShaKeyFor(BASE64.decode(accessTokenSecretKey)))
        .build();

    JwtParser refreshTokenParser = Jwts.parserBuilder()
        .setSigningKey(Keys.hmacShaKeyFor(BASE64.decode(refreshTokenSecretKey)))
        .build();

    SessionRepository sessionRepository = mock(SessionRepository.class);
    JwtUtil jwtUtil = mock(JwtUtil.class);
    SessionPostfixGenerator sessionPostfixGenerator = mock(SessionPostfixGenerator.class);
    SessionServiceImpl sessionService = new SessionServiceImpl(sessionRepository, jwtUtil, sessionPostfixGenerator);

    static class SessionComparator {
        public static boolean compare(Session session1, Session session2) {
            if (
                session1.getSessionKey().getUserID().equals(session2.getSessionKey().getUserID()) &&
                session1.getSessionKey().getSessionPostfix().equals(session2.getSessionKey().getSessionPostfix()) &&
                session1.getRoles().length == session2.getRoles().length &&
                session1.getAccessToken().equals(session2.getAccessToken()) &&
                session1.getRefreshToken().equals(session2.getRefreshToken())
            ) {
                for (String role : session1.getRoles()) {
                    if (!Arrays.asList(session2.getRoles()).contains(role)) return false;
                }

                return true;
            }

            return false;
        }
    }

    @Test
    void createSession() {
        when(jwtUtil.generateAccessTokenBuilder()).thenReturn(generateTestAccessTokenBuilder());
        when(jwtUtil.generateRefreshTokenBuilder()).thenReturn(generateTestRefreshTokenBuilder());
        when(sessionPostfixGenerator.generate()).thenReturn(sessionPostfix);
        when(sessionPostfixGenerator.getPostfixLength()).thenReturn(sessionPostfixLength);
        when(sessionRepository.save(any(Session.class))).then(returnsFirstArg());

        assertDoesNotThrow(() -> sessionService.createSession(expectedUserID, expectedRoles));

        Session actualSession = sessionService.createSession(expectedUserID, expectedRoles);

        assertEquals(expectedUserID, actualSession.getSessionKey().getUserID());
        assertEquals(sessionPostfix, actualSession.getSessionKey().getSessionPostfix());
        assertEquals(1,actualSession.getRoles().length);

        Arrays.stream(expectedRoles).forEach(expectedRole ->
            assertTrue(Arrays.asList(actualSession.getRoles()).contains(expectedRole))
        );

        assertDoesNotThrow(() -> {
            Claims claims = accessTokenParser.parseClaimsJws(actualSession.getAccessToken()).getBody();
            List<String> actualTokenRoles = claims.get("roles", ArrayList.class);

            assertEquals("access", claims.get("test"));
            assertEquals(expectedSessionID, claims.get("sid"));
            assertEquals(expectedUserID, claims.getSubject());
            assertEquals(expectedRoles.length, actualTokenRoles.size());

            Arrays.stream(expectedRoles).forEach(expectedRole ->
                assertTrue(actualTokenRoles.contains(expectedRole))
            );
        });

        assertDoesNotThrow(() -> {
            Claims claims = refreshTokenParser.parseClaimsJws(actualSession.getRefreshToken()).getBody();
            List<String> actualTokenRoles = claims.get("roles", ArrayList.class);

            assertEquals("refresh", claims.get("test"));
            assertEquals(expectedSessionID, claims.get("sid"));
            assertEquals(expectedUserID, claims.getSubject());
            assertEquals(expectedRoles.length, actualTokenRoles.size());

            Arrays.stream(expectedRoles).forEach(expectedRole ->
                assertTrue(actualTokenRoles.contains(expectedRole))
            );
        });
    }

    @Test
    void getSession() {
        Session expectedSession = new Session(
            new SessionKey(expectedUserID,sessionPostfix),
            expectedRoles,
            "a-token",
            "r-token"
        );

        String sessionID =
            expectedSession.getSessionKey().getUserID() +
            '.' +
            expectedSession.getSessionKey().getSessionPostfix();

        String notExistingSessionID =
            "user1" +
            '.' +
            expectedSession.getSessionKey().getSessionPostfix();

        when(sessionPostfixGenerator.generate()).thenReturn(sessionPostfix);
        when(sessionPostfixGenerator.getPostfixLength()).thenReturn(sessionPostfixLength);
        when(sessionRepository.findById(any())).thenReturn(Optional.empty());

        when(sessionRepository.findById(argThat(arg ->
            arg.getUserID().equals(expectedUserID) && arg.getSessionPostfix().equals(sessionPostfix)))
        ).thenReturn(Optional.of(expectedSession));

        assertDoesNotThrow(() -> sessionService.getSession(sessionID));

        Session actualSession = sessionService.getSession(sessionID);

        assertThrows(SessionNotFoundException.class, () -> sessionService.getSession(notExistingSessionID));

        assertEquals(
            expectedSession.getSessionKey().getSessionPostfix(),
            actualSession.getSessionKey().getSessionPostfix()
        );

        assertEquals(
            expectedSession.getSessionKey().getUserID(),
            actualSession.getSessionKey().getUserID()
        );

        assertEquals(
            expectedSession.getRoles().length,
            actualSession.getRoles().length
        );

        Arrays.stream(expectedSession.getRoles()).forEach(expectedRole ->
            assertTrue(Arrays.asList(actualSession.getRoles()).contains(expectedRole))
        );

        assertEquals(
            expectedSession.getAccessToken(),
            actualSession.getAccessToken()
        );

        assertEquals(
            expectedSession.getRefreshToken(),
            actualSession.getRefreshToken()
        );
    }

    @Test
    void getSessionsByUserID() {
        String user1ID = "user1";
        String user2ID = "user2";

        Session session1 = new Session(
            new SessionKey(user1ID,"1111"),
            new String[]{"role1", "role2"},
            "a-token1",
            "r-token1"
        );

        Session session2 = new Session(
            new SessionKey(user1ID,"1112"),
            new String[]{"role"},
            "a-token2",
            "r-token2"
        );

        Session session3 = new Session(
            new SessionKey(user2ID,"1113"),
            new String[]{"role3"},
            "a-token3",
            "r-token3"
        );

        List<Session> expectedSessionsUser1 = List.of(session1, session2);
        List<Session> expectedSessionsUser2 = List.of(session3);

        when(sessionRepository.findAllBySessionKey_UserID(any())).thenReturn(List.of());
        when(sessionRepository.findAllBySessionKey_UserID(eq(user1ID))).thenReturn(expectedSessionsUser1);
        when(sessionRepository.findAllBySessionKey_UserID(eq(user2ID))).thenReturn(expectedSessionsUser2);

        assertThrows(UserSessionsNotFound.class,() -> sessionService.getSessionsByUserID("user3"));

        assertDoesNotThrow(() -> {
            List<Session> actualSessionsUser1 = sessionService.getSessionsByUserID(user1ID);
            assertEquals(expectedSessionsUser1.size(), actualSessionsUser1.size());

            expectedSessionsUser1.forEach(expectedSession ->
                assertTrue(actualSessionsUser1.stream().anyMatch(actualSession ->
                    SessionComparator.compare(expectedSession, actualSession))
                )
            );
        });

        assertDoesNotThrow(() -> {
            List<Session> actualSessionsUser2 = sessionService.getSessionsByUserID(user2ID);
            assertEquals(expectedSessionsUser2.size(), actualSessionsUser2.size());

            expectedSessionsUser2.forEach(expectedSession ->
                assertTrue(actualSessionsUser2.stream().anyMatch(actualSession ->
                    SessionComparator.compare(expectedSession, actualSession))
                )
            );
        });
    }

    @Test
    void refreshSession() {
        String accessToken = Jwts.builder()
            .signWith(Keys.hmacShaKeyFor(BASE64.decode(accessTokenSecretKey)))
            .setSubject(expectedUserID)
            .claim("roles", expectedRoles)
            .claim("sid", expectedSessionID)
            .compact();

        String validRefreshToken = Jwts.builder()
            .signWith(Keys.hmacShaKeyFor(BASE64.decode(refreshTokenSecretKey)))
            .setSubject(expectedUserID)
            .claim("roles", expectedRoles)
            .claim("sid", expectedSessionID)
            .compact();

        String invalidRefreshToken1 = Jwts.builder()
            .signWith(Keys.hmacShaKeyFor(BASE64.decode(refreshTokenSecretKey)))
            .setSubject("u")
            .claim("roles", expectedRoles)
            .claim("sid", "id")
            .compact();

        String invalidRefreshToken2 = "head.payload.signature";

        SessionKey key = new SessionKey(expectedUserID, sessionPostfix);
        Session session = new Session(key, expectedRoles, accessToken, validRefreshToken);

        when(jwtUtil.getRefreshTokenParser()).thenReturn(refreshTokenParser);
        when(jwtUtil.getAccessTokenParser()).thenReturn(accessTokenParser);
        when(jwtUtil.generateAccessTokenBuilder()).thenReturn(generateTestAccessTokenBuilder());
        when(jwtUtil.generateRefreshTokenBuilder()).thenReturn(generateTestRefreshTokenBuilder());
        when(sessionPostfixGenerator.getPostfixLength()).thenReturn(sessionPostfixLength);
        when(sessionRepository.findById(any())).thenReturn(Optional.empty());
        when(sessionRepository.save(any(Session.class))).then(returnsFirstArg());

        when(sessionRepository.findById(argThat(arg ->
            arg.getUserID().equals(expectedUserID) && arg.getSessionPostfix().equals(sessionPostfix)))
        ).thenReturn(Optional.of(session));

        assertDoesNotThrow(() -> {
            Session newSession = sessionService.refreshSession(validRefreshToken);

            assertDoesNotThrow(() -> {
                Claims claims = accessTokenParser.parseClaimsJws(newSession.getAccessToken()).getBody();
                List<String> actualTokenRoles = claims.get("roles", ArrayList.class);

                assertEquals("access", claims.get("test"));
                assertEquals(expectedSessionID, claims.get("sid"));
                assertEquals(expectedUserID, claims.getSubject());
                assertEquals(expectedRoles.length, actualTokenRoles.size());

                Arrays.stream(expectedRoles).forEach(expectedRole ->
                    assertTrue(actualTokenRoles.contains(expectedRole))
                );
            });

            assertDoesNotThrow(() -> {
                Claims claims = refreshTokenParser.parseClaimsJws(newSession.getRefreshToken()).getBody();
                List<String> actualTokenRoles = claims.get("roles", ArrayList.class);

                assertEquals("refresh", claims.get("test"));
                assertEquals(expectedSessionID, claims.get("sid"));
                assertEquals(expectedUserID, claims.getSubject());
                assertEquals(expectedRoles.length, actualTokenRoles.size());

                Arrays.stream(expectedRoles).forEach(expectedRole ->
                    assertTrue(actualTokenRoles.contains(expectedRole))
                );
            });

            assertThrows(InvalidTokenException.class, () -> sessionService.refreshSession(invalidRefreshToken1));
            assertThrows(InvalidTokenException.class, () -> sessionService.refreshSession(invalidRefreshToken2));
        });
    }

    @Test
    void deleteSession() {
        String validRefreshToken = generateTestRefreshTokenBuilder()
            .claim("sid", expectedSessionID)
            .compact();

        String invalidRefreshToken1 = generateTestRefreshTokenBuilder()
            .claim("sid", "notExistingSession")
            .compact();

        String invalidRefreshToken2 = "r-token";

        Session session = new Session(
            new SessionKey(expectedUserID, sessionPostfix),
            expectedRoles,
            "a-token",
            validRefreshToken
        );

        when(sessionPostfixGenerator.getPostfixLength()).thenReturn(sessionPostfixLength);
        when(sessionRepository.findById(any())).thenReturn(Optional.empty());
        when(jwtUtil.getRefreshTokenParser()).thenReturn(refreshTokenParser);

        when(sessionRepository.findById(argThat(arg ->
            arg.getUserID().equals(expectedUserID) && arg.getSessionPostfix().equals(sessionPostfix)))
        ).thenReturn(Optional.of(session));

        assertDoesNotThrow(() -> sessionService.deleteSession(validRefreshToken));
        assertThrows(SessionNotFoundException.class, () -> sessionService.deleteSession(invalidRefreshToken1));
        assertThrows(InvalidTokenException.class, () -> sessionService.deleteSession(invalidRefreshToken2));

        verify(sessionRepository, times(1)).deleteById(argThat(arg ->
            arg.getUserID().equals(expectedUserID) && arg.getSessionPostfix().equals(sessionPostfix)
        ));

        verify(sessionRepository, never()).deleteById(argThat(arg ->
            ! arg.getUserID().equals(expectedUserID) && ! arg.getSessionPostfix().equals(sessionPostfix)
        ));
    }

    @Test
    void deleteSessionsByUserID() {
        String user1ID = "user1";
        String user2ID = "user2";

        Session session1 = new Session(
            new SessionKey(user1ID,"1111"),
            new String[]{"role1", "role2"},
            "a-token1",
            "r-token1"
        );

        Session session2 = new Session(
            new SessionKey(user1ID,"1112"),
            new String[]{"role"},
            "a-token2",
            "r-token2"
        );

        Session session3 = new Session(
            new SessionKey(user2ID,"1113"),
            new String[]{"role3"},
            "a-token3",
            "r-token3"
        );

        List<Session> expectedSessionsUser1 = List.of(session1, session2);
        List<Session> expectedSessionsUser2 = List.of(session3);

        when(sessionRepository.existsAllBySessionKey_UserID(any())).thenReturn(false);
        when(sessionRepository.findAllBySessionKey_UserID(any())).thenReturn(List.of());
        when(sessionRepository.existsAllBySessionKey_UserID(eq(user1ID))).thenReturn(true);
        when(sessionRepository.existsAllBySessionKey_UserID(eq(user2ID))).thenReturn(true);
        when(sessionRepository.findAllBySessionKey_UserID(eq(user1ID))).thenReturn(expectedSessionsUser1);
        when(sessionRepository.findAllBySessionKey_UserID(eq(user2ID))).thenReturn(expectedSessionsUser2);

        assertDoesNotThrow(() -> {
            sessionService.deleteSessionsByUserID(user1ID);
            sessionService.deleteSessionsByUserID(user2ID);
        });

        assertThrows(UserSessionsNotFound.class, () -> sessionService.deleteSessionsByUserID("user3"));

        verify(sessionRepository, times(1)).deleteAllBySessionKey_UserID(eq(user1ID));
        verify(sessionRepository, times(1)).deleteAllBySessionKey_UserID(eq(user2ID));
        verify(sessionRepository, never()).deleteAllBySessionKey_UserID(eq("user3"));

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
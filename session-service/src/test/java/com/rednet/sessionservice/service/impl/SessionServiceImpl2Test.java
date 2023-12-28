package com.rednet.sessionservice.service.impl;

import antlr.Token;
import com.rednet.sessionservice.entity.Session;
import com.rednet.sessionservice.exception.impl.InvalidSessionIDException;
import com.rednet.sessionservice.exception.impl.InvalidTokenException;
import com.rednet.sessionservice.exception.impl.SessionNotFoundException;
import com.rednet.sessionservice.exception.impl.UserSessionsNotFoundException;
import com.rednet.sessionservice.model.SessionID;
import com.rednet.sessionservice.model.TokenClaims;
import com.rednet.sessionservice.repository.SessionRepository;
import com.rednet.sessionservice.service.SessionService;
import com.rednet.sessionservice.service.SessionTokenService;
import com.rednet.sessionservice.util.SessionIDShaper;
import com.rednet.sessionservice.util.TokenIDGenerator;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SessionServiceImpl2Test {
    private final SessionRepository     sessionRepository = mock(SessionRepository.class);
    private final TokenIDGenerator      tokenIDGenerator = mock(TokenIDGenerator.class);
    private final SessionTokenService   sessionTokenService = mock(SessionTokenService.class);
    private final SessionIDShaper       sessionIDShaper = mock(SessionIDShaper.class);

    private final SessionService sut = new SessionServiceImpl2(
            sessionRepository,
            tokenIDGenerator,
            sessionTokenService,
            sessionIDShaper
    );

    @Test
    void Creating_session_is_successful() {
        String      expectedUserID = randString();
        String      expectedSessionKey = randString();
        String      expectedSessionIDStr = randString();
        String      expectedTokenID = randString();
        String      expectedAccessToken = randString();
        String      expectedRefreshToken = randString();
        Instant     expectedCreatedAtAfter = Instant.now();
        String[]    expectedRoles = new String[]{randString()};
        SessionID   expectedSessionID = new SessionID(expectedUserID, expectedSessionKey);

        Session expectedSession = new Session(
                expectedUserID,
                expectedSessionKey,
                expectedCreatedAtAfter,
                expectedRoles,
                expectedAccessToken,
                expectedRefreshToken,
                expectedTokenID
        );

        when(tokenIDGenerator.generate()).thenReturn(expectedTokenID);
        when(sessionIDShaper.generate(any())).thenReturn(expectedSessionID);
        when(sessionIDShaper.convert(any())).thenReturn(expectedSessionIDStr);
        when(sessionTokenService.generateAccessToken(any())).thenReturn(expectedAccessToken);
        when(sessionTokenService.generateRefreshToken(any())).thenReturn(expectedRefreshToken);
        when(sessionRepository.insert(any())).thenReturn(expectedSession);

        Session actualSession = sut.createSession(expectedUserID, expectedRoles);

        assertEquals(expectedSession, actualSession);

        verify(sessionRepository).insert(argThat(session -> sessionEqualsAndCreatedAfter(expectedSession, session)));
    }

    @Test
    public void Getting_session_by_valid_id_is_successful() {
        String      expectedSessionIDStr = randString();
        String      expectedUserID = randString();
        String      expectedSessionKey = randString();
        String      expectedTokenID = randString();
        String      expectedAccessToken = randString();
        String      expectedRefreshToken = randString();
        Instant     expectedCreatedAtAfter = Instant.now();
        String[]    expectedRoles = new String[]{randString()};
        SessionID   expectedSessionID = new SessionID(expectedUserID, expectedSessionKey);

        Session expectedSession = new Session(
                expectedUserID,
                expectedSessionKey,
                expectedCreatedAtAfter,
                expectedRoles,
                expectedAccessToken,
                expectedRefreshToken,
                expectedTokenID
        );

        when(sessionIDShaper.parse(any())).thenReturn(expectedSessionID);
        when(sessionRepository.findByID(any())).thenReturn(Optional.of(expectedSession));

        Session actualSession = sut.getSession(expectedSessionIDStr);

        assertEquals(expectedSession, actualSession);
    }

    @Test
    public void Getting_session_by_invalid_id_is_not_successful() {
        String expectedSessionIDStr = randString();

        when(sessionIDShaper.parse(any())).thenThrow(InvalidSessionIDException.class);

        assertThrows(SessionNotFoundException.class, () -> sut.getSession(expectedSessionIDStr));
    }

    @Test
    public void Getting_sessions_by_existing_user_id_is_successful() {
        List<Session> expectedSessions = randSessionsList();

        when(sessionRepository.findAllByUserID(any())).thenReturn(expectedSessions);

        List<Session> actualSessions = sut.getSessionsByUserID("userID");

        assertEquals(expectedSessions.size(), actualSessions.size());
        assertTrue(new HashSet<>(expectedSessions).containsAll(actualSessions));
        assertTrue(new HashSet<>(actualSessions).containsAll(expectedSessions));
    }

    @Test
    public void Getting_sessions_by_not_existing_user_id_is_not_successful() {
        List<Session> expectedSessions = new ArrayList<>();

        when(sessionRepository.findAllByUserID(any())).thenReturn(expectedSessions);

        assertThrows(UserSessionsNotFoundException.class, () -> sut.getSessionsByUserID("userID"));
    }

    @Test
    public void Refreshing_session_by_valid_token_is_successful() {
        String      expectedSessionIDStr = randString();
        String      expectedUserID = randString();
        String      expectedSessionKey = randString();
        String      expectedTokenID = randString();
        String      expectedAccessToken = randString();
        String      expectedRefreshToken = randString();
        Instant     expectedCreatedAtAfter = Instant.now();
        String[]    expectedRoles = new String[]{randString()};
        SessionID   expectedSessionID = new SessionID(expectedUserID, expectedSessionKey);

        TokenClaims expectedTokenClaims = new TokenClaims(
                expectedUserID,
                expectedSessionIDStr,
                expectedTokenID,
                expectedRoles
        );

        Session expectedSession = new Session(
                expectedUserID,
                expectedSessionKey,
                expectedCreatedAtAfter,
                expectedRoles,
                expectedAccessToken,
                expectedRefreshToken,
                expectedTokenID
        );

        when(sessionTokenService.parse(any())).thenReturn(expectedTokenClaims);
        when(sessionIDShaper.parse(any())).thenReturn(expectedSessionID);
        when(sessionRepository.findByID(any())).thenReturn(Optional.of(expectedSession));
        when(sessionRepository.insert(any())).thenReturn(expectedSession);

        Session actualSession = sut.refreshSession(expectedRefreshToken);

        assertEquals(expectedSession, actualSession);

        verify(sessionRepository).insert(argThat(session -> sessionEqualsAndCreatedAfter(expectedSession, session)));
    }

    @Test
    public void Refreshing_session_with_invalid_token_id_is_not_successful() {
        String      expectedSessionIDStr = randString();
        String      expectedUserID = randString();
        String      expectedSessionKey = randString();
        String      expectedTokenID = randString();
        String      expectedInvalidTokenID = randString();
        String      expectedAccessToken = randString();
        String      expectedRefreshToken = randString();
        Instant     expectedCreatedAtAfter = Instant.now();
        String[]    expectedRoles = new String[]{randString()};
        SessionID   expectedSessionID = new SessionID(expectedUserID, expectedSessionKey);

        TokenClaims expectedTokenClaims = new TokenClaims(
                expectedUserID,
                expectedSessionIDStr,
                expectedInvalidTokenID,
                expectedRoles
        );

        Session expectedSession = new Session(
                expectedUserID,
                expectedSessionKey,
                expectedCreatedAtAfter,
                expectedRoles,
                expectedAccessToken,
                expectedRefreshToken,
                expectedTokenID
        );

        when(sessionTokenService.parse(any())).thenReturn(expectedTokenClaims);
        when(sessionIDShaper.parse(any())).thenReturn(expectedSessionID);
        when(sessionRepository.findByID(any())).thenReturn(Optional.of(expectedSession));

        assertThrows(InvalidTokenException.class, () -> sut.refreshSession(expectedRefreshToken));
    }

    @Test
    public void Refreshing_session_with_invalid_session_id_is_not_successful() {
        String      expectedSessionIDStr = randString();
        String      expectedUserID = randString();
        String      expectedTokenID = randString();
        String      expectedRefreshToken = randString();
        String[]    expectedRoles = new String[]{randString()};

        TokenClaims expectedTokenClaims = new TokenClaims(
                expectedUserID,
                expectedSessionIDStr,
                expectedTokenID,
                expectedRoles
        );

        when(sessionTokenService.parse(any())).thenReturn(expectedTokenClaims);
        when(sessionIDShaper.parse(any())).thenThrow(InvalidSessionIDException.class);

        assertThrows(InvalidTokenException.class, () -> sut.refreshSession(expectedRefreshToken));
    }

    @Test
    public void Refreshing_session_with_not_existing_session_id_is_not_successful() {
        String      expectedSessionIDStr = randString();
        String      expectedUserID = randString();
        String      expectedSessionKey = randString();
        String      expectedTokenID = randString();
        String      expectedRefreshToken = randString();
        String[]    expectedRoles = new String[]{randString()};
        SessionID   expectedSessionID = new SessionID(expectedUserID, expectedSessionKey);

        TokenClaims expectedTokenClaims = new TokenClaims(
                expectedUserID,
                expectedSessionIDStr,
                expectedTokenID,
                expectedRoles
        );

        when(sessionTokenService.parse(any())).thenReturn(expectedTokenClaims);
        when(sessionIDShaper.parse(any())).thenReturn(expectedSessionID);
        when(sessionRepository.findByID(any())).thenReturn(Optional.empty());

        assertThrows(InvalidTokenException.class, () -> sut.refreshSession(expectedRefreshToken));
    }



    private int randStringLength() {
        int stringLengthBound = 200;
        return new Random().nextInt(stringLengthBound - 1) + 1;
    }

    private String randString() {
        return RandomStringUtils.random(randStringLength());
    }
    private String[] randStringArray() {
        int length = new Random().nextInt(5);
        String[] array = new String[length];

        for (int i = 0; i < length; ++i) {
            array[i] = randString();
        }

        return array;
    }

    private boolean sessionEqualsAndCreatedAfter(Session session1, Session session2) {
        return  session1.getUserID().equals(session2.getUserID()) &&
                session1.getSessionKey().equals(session2.getSessionKey()) &&
                session1.getAccessToken().equals(session2.getAccessToken()) &&
                session1.getRefreshToken().equals(session2.getRefreshToken()) &&
                session1.getRoles().length == session2.getRoles().length &&
                new HashSet<>(List.of(session1.getRoles())).containsAll(List.of(session2.getRoles())) &&
                new HashSet<>(List.of(session2.getRoles())).containsAll(List.of(session1.getRoles())) &&
                session2.getCreatedAt().isAfter(session1.getCreatedAt());

    }

    private List<Session> randSessionsList() {
        int sessionsCount = new Random().nextInt(5 - 1) + 1;

        return new ArrayList<>() {{
            for (int i = 0; i < sessionsCount; ++i) {
                add(new Session(
                        randString(),
                        randString(),
                        Instant.now(),
                        randStringArray(),
                        randString(),
                        randString(),
                        randString()
                ));
            }
        }};
    }
}
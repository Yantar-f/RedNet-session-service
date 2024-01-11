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
import org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class SessionServiceImplTest {
    private final SessionRepository     sessionRepository   = mock(SessionRepository.class);
    private final TokenIDGenerator      tokenIDGenerator    = mock(TokenIDGenerator.class);
    private final TokenService          tokenService        = mock(TokenService.class);
    private final SessionIDShaper       sessionIDShaper     = mock(SessionIDShaper.class);

    private final SessionService sut = new SessionServiceImpl(
            sessionRepository,
            tokenIDGenerator,
            tokenService,
            sessionIDShaper
    );

    @Test
    void Creating_session_is_successful() {
        String      expectedSessionIDStr    = randString();
        String      expectedUserID          = randString();
        String      expectedSessionKey      = randString();
        String      expectedTokenID         = randString();
        String      expectedAccessToken     = randString();
        String      expectedRefreshToken    = randString();
        Instant     expectedCreatedAtAfter  = Instant.now();
        String[]    expectedRoles           = new String[]{randString()};
        SessionID   expectedSessionID       = new SessionID(expectedUserID, expectedSessionKey);

        Session expectedSession = new Session(
                expectedUserID,
                expectedSessionKey,
                expectedCreatedAtAfter,
                expectedRoles,
                expectedAccessToken,
                expectedRefreshToken,
                expectedTokenID
        );

        when(tokenIDGenerator.generate())
                .thenReturn(expectedTokenID);

        when(sessionIDShaper.generate(any()))
                .thenReturn(expectedSessionID);

        when(sessionIDShaper.convert(any()))
                .thenReturn(expectedSessionIDStr);

        when(tokenService.generateAccessToken(any()))
                .thenReturn(expectedAccessToken);

        when(tokenService.generateRefreshToken(any()))
                .thenReturn(expectedRefreshToken);

        when(sessionRepository.insert(any()))
                .thenReturn(expectedSession);

        Session actualSession = sut.createSession(expectedUserID, expectedRoles);

        assertEquals(expectedSession, actualSession);

        verify(sessionRepository)
                .insert(argThat(session -> isSessionTheSameButCreatedAfter(expectedSession, session)));
    }

    @Test
    public void Getting_session_by_valid_id_is_successful() {
        String      expectedSessionIDStr    = randString();
        String      expectedUserID          = randString();
        String      expectedSessionKey      = randString();
        String      expectedTokenID         = randString();
        String      expectedAccessToken     = randString();
        String      expectedRefreshToken    = randString();
        Instant     expectedCreatedAtAfter  = Instant.now();
        String[]    expectedRoles           = new String[]{randString()};
        SessionID   expectedSessionID       = new SessionID(expectedUserID, expectedSessionKey);

        Session expectedSession = new Session(
                expectedUserID,
                expectedSessionKey,
                expectedCreatedAtAfter,
                expectedRoles,
                expectedAccessToken,
                expectedRefreshToken,
                expectedTokenID
        );

        when(sessionIDShaper.parse(any()))
                .thenReturn(expectedSessionID);

        when(sessionRepository.findByID(any()))
                .thenReturn(Optional.of(expectedSession));

        Session actualSession = sut.getSession(expectedSessionIDStr);

        assertEquals(expectedSession, actualSession);
    }

    @Test
    public void Getting_session_by_not_existing_session_id_is_not_successful() {
        String      expectedSessionIDStr    = randString();
        String      expectedUserID          = randString();
        String      expectedSessionKey      = randString();
        SessionID   expectedSessionID       = new SessionID(expectedUserID, expectedSessionKey);

        when(sessionIDShaper.parse(any()))
                .thenReturn(expectedSessionID);

        when(sessionRepository.findByID(any()))
                .thenReturn(Optional.empty());

        assertThrows(SessionNotFoundException.class, () -> sut.getSession(expectedSessionIDStr));
    }

    @Test
    public void Getting_session_by_invalid_id_is_not_successful() {
        String expectedSessionIDStr = randString();

        when(sessionIDShaper.parse(any()))
                .thenThrow(InvalidSessionIDException.class);

        assertThrows(SessionNotFoundException.class, () -> sut.getSession(expectedSessionIDStr));
    }

    @Test
    public void Getting_sessions_by_existing_user_id_is_successful() {
        List<Session> expectedSessions = randSessionsList();

        when(sessionRepository.findAllByUserID(any()))
                .thenReturn(expectedSessions);

        List<Session> actualSessions = sut.getSessionsByUserID("userID");

        assertEquals(expectedSessions.size(), actualSessions.size());
        assertTrue(new HashSet<>(expectedSessions).containsAll(actualSessions));
        assertTrue(new HashSet<>(actualSessions).containsAll(expectedSessions));
    }

    @Test
    public void Getting_sessions_by_not_existing_user_id_is_not_successful() {
        List<Session> expectedSessions = new ArrayList<>();

        when(sessionRepository.findAllByUserID(any()))
                .thenReturn(expectedSessions);

        assertThrows(UserSessionsNotFoundException.class, () -> sut.getSessionsByUserID("userID"));
    }

    @Test
    public void Refreshing_session_by_valid_token_is_successful() {
        String      expectedSessionIDStr    = randString();
        String      expectedUserID          = randString();
        String      expectedSessionKey      = randString();
        String      expectedTokenID         = randString();
        String      expectedNewTokenID      = randString();
        String      expectedAccessToken     = randString();
        String      expectedNewAccessToken  = randString();
        String      expectedRefreshToken    = randString();
        String      expectedNewRefreshToken = randString();
        String[]    expectedRoles           = new String[]{randString()};
        SessionID   expectedSessionID       = new SessionID(expectedUserID, expectedSessionKey);

        TokenClaims expectedTokenClaims = new TokenClaims(
                expectedUserID,
                expectedSessionIDStr,
                expectedTokenID,
                expectedRoles
        );

        TokenClaims expectedRefreshedTokenClaims = new TokenClaims(
                expectedUserID,
                expectedSessionIDStr,
                expectedNewTokenID,
                expectedRoles
        );

        Session expectedSession = new Session(
                expectedUserID,
                expectedSessionKey,
                Instant.now(),
                expectedRoles,
                expectedAccessToken,
                expectedRefreshToken,
                expectedTokenID
        );

        Session expectedRefreshedSession = new Session(
                expectedUserID,
                expectedSessionKey,
                Instant.now(),
                expectedRoles,
                expectedNewAccessToken,
                expectedNewRefreshToken,
                expectedNewTokenID
        );

        when(tokenService.parseRefreshToken(eq(expectedRefreshToken)))
                .thenReturn(expectedTokenClaims);

        when(sessionIDShaper.parse(eq(expectedSessionIDStr)))
                .thenReturn(expectedSessionID);

        when(sessionRepository.findByID(eq(expectedSessionID)))
                .thenReturn(Optional.of(expectedSession));

        when(tokenIDGenerator.generate())
                .thenReturn(expectedNewTokenID);

        when(tokenService.generateAccessToken(eq(expectedRefreshedTokenClaims)))
                .thenReturn(expectedNewAccessToken);

        when(tokenService.generateRefreshToken(eq(expectedRefreshedTokenClaims)))
                .thenReturn(expectedNewRefreshToken);

        when(sessionRepository.insert(any()))
                .then(returnsFirstArg());

        Session actualSession = sut.refreshSession(expectedRefreshToken);

        assertTrue(isSessionTheSameButCreatedAfter(expectedRefreshedSession, actualSession));

        verify(sessionRepository)
                .insert(argThat(session -> isSessionTheSameButCreatedAfter(expectedRefreshedSession, session)));
    }

    @Test
    public void Refreshing_session_by_invalid_token_id_is_not_successful() {
        String expectedRefreshToken = randString();

        when(tokenService.parseRefreshToken(eq(expectedRefreshToken)))
                .thenThrow(InvalidTokenException.class);

        assertThrows(InvalidTokenException.class, () -> sut.refreshSession(expectedRefreshToken));
    }

    @Test
    public void Refreshing_session_by_invalid_session_id_is_not_successful() {
        String      expectedSessionIDStr    = randString();
        String      expectedUserID          = randString();
        String      expectedTokenID         = randString();
        String      expectedRefreshToken    = randString();
        String[]    expectedRoles           = new String[]{randString()};

        TokenClaims expectedTokenClaims = new TokenClaims(
                expectedUserID,
                expectedSessionIDStr,
                expectedTokenID,
                expectedRoles
        );

        when(tokenService.parseRefreshToken(eq(expectedRefreshToken)))
                .thenReturn(expectedTokenClaims);

        when(sessionIDShaper.parse(eq(expectedSessionIDStr)))
                .thenThrow(InvalidSessionIDException.class);

        assertThrows(InvalidTokenException.class, () -> sut.refreshSession(expectedRefreshToken));
    }

    @Test
    public void Refreshing_session_with_invalid_token_id_is_not_successful() {
        String      expectedSessionIDStr    = randString();
        String      expectedUserID          = randString();
        String      expectedSessionKey      = randString();
        String      expectedTokenID         = randString();
        String      expectedInvalidTokenID  = randString();
        String      expectedAccessToken     = randString();
        String      expectedRefreshToken    = randString();
        String[]    expectedRoles           = new String[]{randString()};
        SessionID   expectedSessionID       = new SessionID(expectedUserID, expectedSessionKey);

        TokenClaims expectedTokenClaims = new TokenClaims(
                expectedUserID,
                expectedSessionIDStr,
                expectedInvalidTokenID,
                expectedRoles
        );

        Session expectedSession = new Session(
                expectedUserID,
                expectedSessionKey,
                Instant.now(),
                expectedRoles,
                expectedAccessToken,
                expectedRefreshToken,
                expectedTokenID
        );

        when(tokenService.parseRefreshToken(eq(expectedRefreshToken)))
                .thenReturn(expectedTokenClaims);

        when(sessionIDShaper.parse(eq(expectedSessionIDStr)))
                .thenReturn(expectedSessionID);

        when(sessionRepository.findByID(eq(expectedSessionID)))
                .thenReturn(Optional.of(expectedSession));

        assertThrows(InvalidTokenException.class, () -> sut.refreshSession(expectedRefreshToken));
    }

    @Test
    public void Refreshing_session_by_not_existing_session_id_is_not_successful() {
        String      expectedSessionIDStr    = randString();
        String      expectedUserID          = randString();
        String      expectedSessionKey      = randString();
        String      expectedTokenID         = randString();
        String      expectedRefreshToken    = randString();
        String[]    expectedRoles           = new String[]{randString()};
        SessionID   expectedSessionID       = new SessionID(expectedUserID, expectedSessionKey);

        TokenClaims expectedTokenClaims = new TokenClaims(
                expectedUserID,
                expectedSessionIDStr,
                expectedTokenID,
                expectedRoles
        );

        when(tokenService.parseRefreshToken(expectedRefreshToken))
                .thenReturn(expectedTokenClaims);

        when(sessionIDShaper.parse(any()))
                .thenReturn(expectedSessionID);

        when(sessionRepository.findByID(any()))
                .thenReturn(Optional.empty());

        assertThrows(InvalidTokenException.class, () -> sut.refreshSession(expectedRefreshToken));
    }

    @Test
    public void Deleting_session_by_valid_token_is_successful() {
        String      expectedSessionIDStr    = randString();
        String      expectedUserID          = randString();
        String      expectedSessionKey      = randString();
        String      expectedTokenID         = randString();
        String      expectedAccessToken     = randString();
        String      expectedRefreshToken    = randString();
        String[]    expectedRoles           = new String[]{randString()};
        SessionID   expectedSessionID       = new SessionID(expectedUserID, expectedSessionKey);

        TokenClaims expectedTokenClaims = new TokenClaims(
                expectedUserID,
                expectedSessionIDStr,
                expectedTokenID,
                expectedRoles
        );

        Session expectedSession = new Session(
                expectedUserID,
                expectedSessionKey,
                Instant.now(),
                expectedRoles,
                expectedAccessToken,
                expectedRefreshToken,
                expectedTokenID
        );

        when(tokenService.parseRefreshToken(eq(expectedRefreshToken)))
                .thenReturn(expectedTokenClaims);

        when(sessionIDShaper.parse(eq(expectedSessionIDStr)))
                .thenReturn(expectedSessionID);

        when(sessionRepository.findByID(eq(expectedSessionID)))
                .thenReturn(Optional.of(expectedSession));

        when(sessionRepository.deleteByID(eq(expectedSessionID)))
                .thenReturn(true);

        sut.deleteSession(expectedRefreshToken);

        verify(sessionRepository)
                .deleteByID(eq(expectedSessionID));
    }

    @Test
    public void Deleting_session_by_invalid_token_is_not_successful() {
        String expectedRefreshToken = randString();

        when(tokenService.parseRefreshToken(eq(expectedRefreshToken)))
                .thenThrow(InvalidTokenException.class);

        assertThrows(InvalidTokenException.class, () -> sut.deleteSession(expectedRefreshToken));
    }

    @Test
    public void Deleting_session_by_invalid_session_id_is_not_successful() {
        String expectedRefreshToken = randString();
        String expectedSessionIDStr = randString();
        String expectedUserID = randString();
        String expectedTokenID = randString();
        String[] expectedRoles = new String[]{randString()};

        TokenClaims expectedTokenClaims = new TokenClaims(
                expectedUserID,
                expectedSessionIDStr,
                expectedTokenID,
                expectedRoles
        );

        when(tokenService.parseRefreshToken(eq(expectedRefreshToken)))
                .thenReturn(expectedTokenClaims);

        when(sessionIDShaper.parse(eq(expectedSessionIDStr)))
                .thenThrow(InvalidSessionIDException.class);

        assertThrows(InvalidTokenException.class, () -> sut.deleteSession(expectedRefreshToken));
    }

    @Test
    public void Deleting_session_by_not_existing_session_id_is_not_successful() {
        String      expectedSessionIDStr    = randString();
        String      expectedUserID          = randString();
        String      expectedSessionKey      = randString();
        String      expectedTokenID         = randString();
        String      expectedRefreshToken    = randString();
        String[]    expectedRoles           = new String[]{randString()};
        SessionID   expectedSessionID       = new SessionID(expectedUserID, expectedSessionKey);

        TokenClaims expectedTokenClaims = new TokenClaims(
                expectedUserID,
                expectedSessionIDStr,
                expectedTokenID,
                expectedRoles
        );

        when(tokenService.parseRefreshToken(eq(expectedRefreshToken)))
                .thenReturn(expectedTokenClaims);

        when(sessionIDShaper.parse(eq(expectedSessionIDStr)))
                .thenReturn(expectedSessionID);

        when(sessionRepository.findByID(any()))
                .thenReturn(Optional.empty());

        assertThrows(InvalidTokenException.class, () -> sut.deleteSession(expectedRefreshToken));
    }

    @Test
    public void Deleting_session_with_error_is_not_successful() {
        String      expectedSessionIDStr    = randString();
        String      expectedUserID          = randString();
        String      expectedSessionKey      = randString();
        String      expectedTokenID         = randString();
        String      expectedAccessToken     = randString();
        String      expectedRefreshToken    = randString();
        String[]    expectedRoles           = new String[]{randString()};
        SessionID   expectedSessionID       = new SessionID(expectedUserID, expectedSessionKey);

        TokenClaims expectedTokenClaims = new TokenClaims(
                expectedUserID,
                expectedSessionIDStr,
                expectedTokenID,
                expectedRoles
        );

        Session expectedSession = new Session(
                expectedUserID,
                expectedSessionKey,
                Instant.now(),
                expectedRoles,
                expectedAccessToken,
                expectedRefreshToken,
                expectedTokenID
        );

        when(tokenService.parseRefreshToken(eq(expectedRefreshToken)))
                .thenReturn(expectedTokenClaims);

        when(sessionIDShaper.parse(eq(expectedSessionIDStr)))
                .thenReturn(expectedSessionID);

        when(sessionRepository.findByID(any()))
                .thenReturn(Optional.of(expectedSession));

        when(sessionRepository.deleteByID(eq(expectedSessionID)))
                .thenReturn(false);

        assertThrows(SessionRemovingException.class, () -> sut.deleteSession(expectedRefreshToken));

        verify(sessionRepository)
                .deleteByID(eq(expectedSessionID));
    }

    @Test
    public void Deleting_user_sessions_by_existing_user_id_is_successful() {
        String expectedUserID = randString();

        when(sessionRepository.existsByUserID(eq(expectedUserID)))
                .thenReturn(true);

        when(sessionRepository.deleteAllByUserID(eq(expectedUserID)))
                .thenReturn(true);

        sut.deleteSessionsByUserID(expectedUserID);

        verify(sessionRepository)
                .deleteAllByUserID(eq(expectedUserID));
    }

    @Test
    public void Deleting_user_sessions_by_not_existing_user_id_is_not_successful() {
        String expectedUserID = randString();

        when(sessionRepository.existsByUserID(eq(expectedUserID)))
                .thenReturn(false);

        when(sessionRepository.deleteAllByUserID(eq(expectedUserID)))
                .thenReturn(true);

        assertThrows(UserSessionsNotFoundException.class, () -> sut.deleteSessionsByUserID(expectedUserID));

        verify(sessionRepository, never())
                .deleteAllByUserID(eq(expectedUserID));
    }

    @Test
    public void Deleting_user_sessions_with_error_is_not_successful() {
        String expectedUserID = randString();

        when(sessionRepository.existsByUserID(eq(expectedUserID)))
                .thenReturn(true);

        when(sessionRepository.deleteAllByUserID(eq(expectedUserID)))
                .thenReturn(false);

        assertThrows(UserSessionsRemovingException.class, () -> sut.deleteSessionsByUserID(expectedUserID));

        verify(sessionRepository)
                .deleteAllByUserID(eq(expectedUserID));
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

    private boolean isSessionTheSameButCreatedAfter(Session session1, Session session2) {
        return  session1.getUserID().equals(session2.getUserID()) &&
                session1.getSessionKey().equals(session2.getSessionKey()) &&
                session1.getTokenID().equals(session2.getTokenID()) &&
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
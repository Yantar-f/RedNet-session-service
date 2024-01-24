package com.rednet.sessionservice.service.impl;

import com.rednet.sessionservice.entity.Session;
import com.rednet.sessionservice.exception.impl.InvalidSessionIDException;
import com.rednet.sessionservice.exception.impl.InvalidTokenException;
import com.rednet.sessionservice.exception.impl.SessionNotFoundException;
import com.rednet.sessionservice.exception.impl.SessionRemovingException;
import com.rednet.sessionservice.exception.impl.UserSessionsNotFoundException;
import com.rednet.sessionservice.exception.impl.UserSessionsRemovingException;
import com.rednet.sessionservice.model.SessionID;
import com.rednet.sessionservice.model.TokenClaims;
import com.rednet.sessionservice.repository.SessionRepository;
import com.rednet.sessionservice.service.SessionService;
import com.rednet.sessionservice.util.SessionIDShaper;
import com.rednet.sessionservice.util.TokenIDGenerator;
import com.rednet.sessionservice.util.TokenUtil;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.instancio.Instancio.create;
import static org.instancio.Instancio.ofList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SessionServiceImplTest {
    private final SessionRepository     sessionRepository = mock(SessionRepository.class);
    private final TokenIDGenerator      tokenIDGenerator = mock(TokenIDGenerator.class);
    private final TokenUtil tokenUtil = mock(TokenUtil.class);
    private final SessionIDShaper       sessionIDShaper = mock(SessionIDShaper.class);

    private final SessionService sut = new SessionServiceImpl(
            sessionRepository,
            tokenIDGenerator,
            tokenUtil,
            sessionIDShaper
    );

    @Test
    void Creating_session_is_successful() {
        String      expectedSessionIDStr = create(String.class);
        String      expectedUserID = create(String.class);
        String      expectedSessionKey = create(String.class);
        String      expectedTokenID = create(String.class);
        String      expectedAccessToken = create(String.class);
        String      expectedRefreshToken = create(String.class);
        Instant     expectedCreatedAtAfter = Instant.now();
        String[]    expectedRoles = create(String[].class);
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

        when(tokenIDGenerator.generate())
                .thenReturn(expectedTokenID);

        when(sessionIDShaper.generate(any()))
                .thenReturn(expectedSessionID);

        when(sessionIDShaper.convert(any()))
                .thenReturn(expectedSessionIDStr);

        when(tokenUtil.generateAccessToken(any()))
                .thenReturn(expectedAccessToken);

        when(tokenUtil.generateRefreshToken(any()))
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
        String      expectedSessionIDStr = create(String.class);
        String      expectedUserID = create(String.class);
        String      expectedSessionKey = create(String.class);
        String      expectedTokenID = create(String.class);
        String      expectedAccessToken = create(String.class);
        String      expectedRefreshToken = create(String.class);
        Instant     expectedCreatedAtAfter = Instant.now();
        String[]    expectedRoles = create(String[].class);
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

        when(sessionIDShaper.parse(any()))
                .thenReturn(expectedSessionID);

        when(sessionRepository.findByID(any()))
                .thenReturn(Optional.of(expectedSession));

        Session actualSession = sut.getSession(expectedSessionIDStr);

        assertEquals(expectedSession, actualSession);
    }

    @Test
    public void Getting_session_by_not_existing_session_id_is_not_successful() {
        String      expectedSessionIDStr = create(String.class);
        String      expectedUserID = create(String.class);
        String      expectedSessionKey = create(String.class);
        SessionID   expectedSessionID = new SessionID(expectedUserID, expectedSessionKey);

        when(sessionIDShaper.parse(any()))
                .thenReturn(expectedSessionID);

        when(sessionRepository.findByID(any()))
                .thenReturn(Optional.empty());

        assertThrows(SessionNotFoundException.class, () -> sut.getSession(expectedSessionIDStr));
    }

    @Test
    public void Getting_session_by_invalid_id_is_not_successful() {
        String expectedSessionIDStr = create(String.class);;

        when(sessionIDShaper.parse(any()))
                .thenThrow(InvalidSessionIDException.class);

        assertThrows(SessionNotFoundException.class, () -> sut.getSession(expectedSessionIDStr));
    }

    @Test
    public void Getting_sessions_by_existing_user_id_is_successful() {
        String expectedUserID = create(String.class);
        List<Session> expectedSessions = ofList(Session.class).create();

        when(sessionRepository.findAllByUserID(eq(expectedUserID)))
                .thenReturn(expectedSessions);

        List<Session> actualSessions = sut.getSessionsByUserID(expectedUserID);

        assertEquals(expectedSessions.size(), actualSessions.size());
        assertTrue(new HashSet<>(expectedSessions).containsAll(actualSessions));
        assertTrue(new HashSet<>(actualSessions).containsAll(expectedSessions));
    }

    @Test
    public void Getting_sessions_by_not_existing_user_id_is_not_successful() {
        String expectedUserID = create(String.class);
        List<Session> expectedSessions = new ArrayList<>();

        when(sessionRepository.findAllByUserID(eq(expectedUserID)))
                .thenReturn(expectedSessions);

        assertThrows(UserSessionsNotFoundException.class, () -> sut.getSessionsByUserID(expectedUserID));
    }

    @Test
    public void Refreshing_session_by_valid_token_is_successful() {
        String      expectedSessionIDStr = create(String.class);
        String      expectedUserID = create(String.class);
        String      expectedSessionKey = create(String.class);
        String      expectedTokenID = create(String.class);
        String      expectedNewTokenID = create(String.class);
        String      expectedAccessToken = create(String.class);
        String      expectedNewAccessToken = create(String.class);
        String      expectedRefreshToken = create(String.class);
        String      expectedNewRefreshToken = create(String.class);
        String[]    expectedRoles = create(String[].class);
        SessionID   expectedSessionID = new SessionID(expectedUserID, expectedSessionKey);

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

        when(tokenUtil.parseRefreshToken(eq(expectedRefreshToken)))
                .thenReturn(expectedTokenClaims);

        when(sessionIDShaper.parse(eq(expectedSessionIDStr)))
                .thenReturn(expectedSessionID);

        when(sessionRepository.findByID(eq(expectedSessionID)))
                .thenReturn(Optional.of(expectedSession));

        when(tokenIDGenerator.generate())
                .thenReturn(expectedNewTokenID);

        when(tokenUtil.generateAccessToken(eq(expectedRefreshedTokenClaims)))
                .thenReturn(expectedNewAccessToken);

        when(tokenUtil.generateRefreshToken(eq(expectedRefreshedTokenClaims)))
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
        String expectedRefreshToken = create(String.class);;

        when(tokenUtil.parseRefreshToken(eq(expectedRefreshToken)))
                .thenThrow(InvalidTokenException.class);

        assertThrows(InvalidTokenException.class, () -> sut.refreshSession(expectedRefreshToken));
    }

    @Test
    public void Refreshing_session_by_invalid_session_id_is_not_successful() {
        String      expectedSessionIDStr = create(String.class);
        String      expectedUserID = create(String.class);
        String      expectedTokenID = create(String.class);
        String      expectedRefreshToken = create(String.class);
        String[]    expectedRoles = create(String[].class);

        TokenClaims expectedTokenClaims = new TokenClaims(
                expectedUserID,
                expectedSessionIDStr,
                expectedTokenID,
                expectedRoles
        );

        when(tokenUtil.parseRefreshToken(eq(expectedRefreshToken)))
                .thenReturn(expectedTokenClaims);

        when(sessionIDShaper.parse(eq(expectedSessionIDStr)))
                .thenThrow(InvalidSessionIDException.class);

        assertThrows(InvalidTokenException.class, () -> sut.refreshSession(expectedRefreshToken));
    }

    @Test
    public void Refreshing_session_with_invalid_token_id_is_not_successful() {
        String      expectedSessionIDStr = create(String.class);
        String      expectedUserID = create(String.class);
        String      expectedSessionKey = create(String.class);
        String      expectedTokenID = create(String.class);
        String      expectedInvalidTokenID  = create(String.class);
        String      expectedAccessToken = create(String.class);
        String      expectedRefreshToken = create(String.class);
        String[]    expectedRoles = create(String[].class);
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
                Instant.now(),
                expectedRoles,
                expectedAccessToken,
                expectedRefreshToken,
                expectedTokenID
        );

        when(tokenUtil.parseRefreshToken(eq(expectedRefreshToken)))
                .thenReturn(expectedTokenClaims);

        when(sessionIDShaper.parse(eq(expectedSessionIDStr)))
                .thenReturn(expectedSessionID);

        when(sessionRepository.findByID(eq(expectedSessionID)))
                .thenReturn(Optional.of(expectedSession));

        assertThrows(InvalidTokenException.class, () -> sut.refreshSession(expectedRefreshToken));
    }

    @Test
    public void Refreshing_session_by_not_existing_session_id_is_not_successful() {
        String      expectedSessionIDStr = create(String.class);
        String      expectedUserID = create(String.class);
        String      expectedSessionKey = create(String.class);
        String      expectedTokenID = create(String.class);
        String      expectedRefreshToken = create(String.class);
        String[]    expectedRoles = create(String[].class);
        SessionID   expectedSessionID = new SessionID(expectedUserID, expectedSessionKey);

        TokenClaims expectedTokenClaims = new TokenClaims(
                expectedUserID,
                expectedSessionIDStr,
                expectedTokenID,
                expectedRoles
        );

        when(tokenUtil.parseRefreshToken(expectedRefreshToken))
                .thenReturn(expectedTokenClaims);

        when(sessionIDShaper.parse(any()))
                .thenReturn(expectedSessionID);

        when(sessionRepository.findByID(any()))
                .thenReturn(Optional.empty());

        assertThrows(InvalidTokenException.class, () -> sut.refreshSession(expectedRefreshToken));
    }

    @Test
    public void Deleting_session_by_valid_token_is_successful() {
        String      expectedSessionIDStr = create(String.class);
        String      expectedUserID = create(String.class);
        String      expectedSessionKey = create(String.class);
        String      expectedTokenID = create(String.class);
        String      expectedAccessToken = create(String.class);
        String      expectedRefreshToken = create(String.class);
        String[]    expectedRoles = create(String[].class);
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
                Instant.now(),
                expectedRoles,
                expectedAccessToken,
                expectedRefreshToken,
                expectedTokenID
        );

        when(tokenUtil.parseRefreshToken(eq(expectedRefreshToken)))
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
        String expectedRefreshToken = create(String.class);

        when(tokenUtil.parseRefreshToken(eq(expectedRefreshToken)))
                .thenThrow(InvalidTokenException.class);

        assertThrows(InvalidTokenException.class, () -> sut.deleteSession(expectedRefreshToken));
    }

    @Test
    public void Deleting_session_by_invalid_session_id_is_not_successful() {
        String      expectedRefreshToken = create(String.class);
        String      expectedSessionIDStr = create(String.class);
        String      expectedUserID = create(String.class);
        String      expectedTokenID = create(String.class);
        String[]    expectedRoles = create(String[].class);

        TokenClaims expectedTokenClaims = new TokenClaims(
                expectedUserID,
                expectedSessionIDStr,
                expectedTokenID,
                expectedRoles
        );

        when(tokenUtil.parseRefreshToken(eq(expectedRefreshToken)))
                .thenReturn(expectedTokenClaims);

        when(sessionIDShaper.parse(eq(expectedSessionIDStr)))
                .thenThrow(InvalidSessionIDException.class);

        assertThrows(InvalidTokenException.class, () -> sut.deleteSession(expectedRefreshToken));
    }

    @Test
    public void Deleting_session_by_not_existing_session_id_is_not_successful() {
        String      expectedSessionIDStr = create(String.class);
        String      expectedUserID = create(String.class);
        String      expectedSessionKey = create(String.class);
        String      expectedTokenID = create(String.class);
        String      expectedRefreshToken = create(String.class);
        String[]    expectedRoles = create(String[].class);
        SessionID   expectedSessionID = new SessionID(expectedUserID, expectedSessionKey);

        TokenClaims expectedTokenClaims = new TokenClaims(
                expectedUserID,
                expectedSessionIDStr,
                expectedTokenID,
                expectedRoles
        );

        when(tokenUtil.parseRefreshToken(eq(expectedRefreshToken)))
                .thenReturn(expectedTokenClaims);

        when(sessionIDShaper.parse(eq(expectedSessionIDStr)))
                .thenReturn(expectedSessionID);

        when(sessionRepository.findByID(any()))
                .thenReturn(Optional.empty());

        assertThrows(InvalidTokenException.class, () -> sut.deleteSession(expectedRefreshToken));
    }

    @Test
    public void Deleting_session_with_error_is_not_successful() {
        String      expectedSessionIDStr = create(String.class);
        String      expectedUserID = create(String.class);
        String      expectedSessionKey = create(String.class);
        String      expectedTokenID = create(String.class);
        String      expectedAccessToken = create(String.class);
        String      expectedRefreshToken = create(String.class);
        String[]    expectedRoles = create(String[].class);
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
                Instant.now(),
                expectedRoles,
                expectedAccessToken,
                expectedRefreshToken,
                expectedTokenID
        );

        when(tokenUtil.parseRefreshToken(eq(expectedRefreshToken)))
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
        String expectedUserID = create(String.class);

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
        String expectedUserID = create(String.class);

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
        String expectedUserID = create(String.class);

        when(sessionRepository.existsByUserID(eq(expectedUserID)))
                .thenReturn(true);

        when(sessionRepository.deleteAllByUserID(eq(expectedUserID)))
                .thenReturn(false);

        assertThrows(UserSessionsRemovingException.class, () -> sut.deleteSessionsByUserID(expectedUserID));

        verify(sessionRepository)
                .deleteAllByUserID(eq(expectedUserID));
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
}
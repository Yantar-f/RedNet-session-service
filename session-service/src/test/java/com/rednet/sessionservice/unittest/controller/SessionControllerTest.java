package com.rednet.sessionservice.unittest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rednet.sessionservice.controller.SessionController;
import com.rednet.sessionservice.entity.Session;
import com.rednet.sessionservice.exception.handler.GlobalExceptionHandler;
import com.rednet.sessionservice.exception.impl.InvalidTokenException;
import com.rednet.sessionservice.exception.impl.SessionNotFoundException;
import com.rednet.sessionservice.exception.impl.UserSessionsNotFoundException;
import com.rednet.sessionservice.model.SessionCreationData;
import com.rednet.sessionservice.model.SessionRefreshingData;
import com.rednet.sessionservice.service.SessionService;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(useDefaultFilters = false, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(print = MockMvcPrint.SYSTEM_OUT, addFilters = false)
@Import({SessionController.class, GlobalExceptionHandler.class})
class SessionControllerTest {
    @MockBean
    SessionService sessionService;

    @Autowired
    SessionController sessionController;

    @Autowired
    GlobalExceptionHandler globalExceptionHandler;

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    public void Creating_session_is_successful() throws Exception {
        Session expectedSession = Instancio.create(Session.class);

        SessionCreationData creationData = new SessionCreationData(
                expectedSession.getUserID(),
                expectedSession.getRoles()
        );

        when(sessionService.createSession(eq(creationData)))
                .thenReturn(expectedSession);

        MvcResult result = mvc.perform(
                post("/sessions", creationData)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creationData)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andReturn();

        verify(sessionService)
                .createSession(eq(creationData));

        String responseBody = result.getResponse().getContentAsString();
        Session actualSession = objectMapper.readValue(responseBody, Session.class);

        assertEquals(expectedSession, actualSession);
    }

    @Test
    public void Creating_session_with_invalid_content_type_is_not_successful() throws Exception {
        SessionCreationData creationData = Instancio.create(SessionCreationData.class);

        mvc.perform(
                post("/sessions", creationData)
                        .content(objectMapper.writeValueAsString(creationData)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON));

        verify(sessionService, never())
                .createSession(any());
    }

    @Test
    public void Creating_session_with_nullable_user_id_is_not_successful() throws Exception {
        SessionCreationData creationData = new SessionCreationData(
                null,
                Instancio.create(String[].class)
        );

        mvc.perform(post("/sessions", creationData)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creationData)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON));

        verify(sessionService, never())
                .createSession(any());
    }

    @Test
    public void Creating_session_with_blank_user_id_is_not_successful() throws Exception {
        SessionCreationData creationData = new SessionCreationData(
                "",
                Instancio.create(String[].class)
        );

        mvc.perform(post("/sessions", creationData)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creationData)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON));

        verify(sessionService, never())
                .createSession(any());
    }

    @Test
    public void Creating_session_with_nullable_roles_is_not_successful() throws Exception {
        SessionCreationData creationData = new SessionCreationData(
                Instancio.create(String.class),
                null
        );

        mvc.perform(post("/sessions", creationData)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creationData)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON));

        verify(sessionService, never())
                .createSession(any());
    }

    @Test
    public void Creating_session_with_blank_roles_is_not_successful() throws Exception {
        SessionCreationData creationData = new SessionCreationData(
                Instancio.create(String.class),
                new String[]{}
        );

        mvc.perform(post("/sessions", creationData)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creationData)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON));

        verify(sessionService, never())
                .createSession(any());
    }

    @Test
    public void Getting_session_is_successful() throws Exception {
        Session expectedSession = Instancio.create(Session.class);
        String expectedSessionID = Instancio.create(String.class);

        when(sessionService.getSession(eq(expectedSessionID)))
                .thenReturn(expectedSession);

        MvcResult result = mvc.perform(
                get("/sessions/by-id")
                        .param("id", expectedSessionID))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        Session actualSession = objectMapper.readValue(responseBody, Session.class);

        assertEquals(expectedSession, actualSession);
    }

    @Test
    public void Getting_session_with_blank_id_is_not_successful() throws Exception {
        mvc.perform(get("/sessions/by-id")
                        .param("id", ""))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON));
    }

    @Test
    public void Getting_session_with_invalid_id_is_not_successful() throws Exception {
        String expectedSessionID = Instancio.create(String.class);

        when(sessionService.getSession(eq(expectedSessionID)))
                .thenThrow(SessionNotFoundException.class);

        mvc.perform(get("/sessions/by-id")
                        .param("id", expectedSessionID))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_JSON));
    }

    @Test
    public void Getting_session_by_user_id_is_successful() throws Exception {
        String expectedUserID = Instancio.create(String.class);
        List<Session> expectedSessions = Instancio.ofList(Session.class).create();

        when(sessionService.getSessionsByUserID(eq(expectedUserID)))
                .thenReturn(expectedSessions);

        MvcResult result = mvc.perform(get("/sessions/by-user-id")
                        .param("user-id", expectedUserID))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        Session[] sessions = objectMapper.readValue(responseBody, Session[].class);
        List<Session> actualSessions =List.of(sessions);

        assertEquals(expectedSessions.size(), actualSessions.size());
        assertTrue(new HashSet<>(expectedSessions).containsAll(actualSessions));
    }

    @Test
    public void Getting_session_with_blank_user_id_is_not_successful() throws Exception {
        mvc.perform(get("/sessions/by-user-id")
                        .param("user-id", ""))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON));
    }

    @Test
    public void Getting_session_with_invalid_user_id_is_not_successful() throws Exception {
        String expectedUserID = Instancio.create(String.class);

        when(sessionService.getSessionsByUserID(eq(expectedUserID)))
                .thenThrow(UserSessionsNotFoundException.class);

        mvc.perform(get("/sessions/by-user-id")
                        .param("user-id", expectedUserID))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_JSON));
    }

    @Test
    public void Updating_session_is_successful() throws Exception {
        SessionRefreshingData expectedRefreshingData = Instancio.create(SessionRefreshingData.class);
        Session expectedUpdatedSession = Instancio.create(Session.class);

        when(sessionService.refreshSession(eq(expectedRefreshingData.refreshToken())))
                .thenReturn(expectedUpdatedSession);

        MvcResult result = mvc.perform(put("/sessions")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expectedRefreshingData)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        Session actualSession = objectMapper.readValue(responseBody, Session.class);

        assertEquals(expectedUpdatedSession, actualSession);

        verify(sessionService)
                .refreshSession(eq(expectedRefreshingData.refreshToken()));
    }

    @Test
    public void Updating_session_with_invalid_content_type_is_not_successful() throws Exception {
        SessionRefreshingData expectedRefreshingData = Instancio.create(SessionRefreshingData.class);

        mvc.perform(put("/sessions")
                        .content(objectMapper.writeValueAsString(expectedRefreshingData)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON));

        verify(sessionService, never())
                .refreshSession(any());
    }

    @Test
    public void Updating_session_with_blank_data_is_not_successful() throws Exception {
        SessionRefreshingData refreshingData = new SessionRefreshingData("");

        mvc.perform(put("/sessions")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshingData)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON));

        verify(sessionService, never())
                .refreshSession(any());
    }

    @Test
    public void Updating_session_with_nullable_data_is_not_successful() throws Exception {
        SessionRefreshingData refreshingData = new SessionRefreshingData(null);

        mvc.perform(put("/sessions")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshingData)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON));

        verify(sessionService, never())
                .refreshSession(any());
    }

    @Test
    public void Updating_session_with_invalid_token_is_not_successful() throws Exception {
        SessionRefreshingData refreshingData = Instancio.create(SessionRefreshingData.class);

        when(sessionService.refreshSession(eq(refreshingData.refreshToken())))
                .thenThrow(InvalidTokenException.class);

        mvc.perform(put("/sessions")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshingData)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON));
    }

    @Test
    public void Deleting_sessions_by_user_id_is_successful() throws Exception {
        String expectedUserID = Instancio.create(String.class);

        mvc.perform(delete("/sessions/by-user-id")
                        .param("user-id", expectedUserID))
                .andExpect(status().is2xxSuccessful());

        verify(sessionService)
                .deleteSessionsByUserID(eq(expectedUserID));
    }

    @Test
    public void Deleting_sessions_with_blank_user_id_is_not_successful() throws Exception {
        mvc.perform(delete("/sessions/by-user-id")
                        .param("user-id", ""))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON));

        verify(sessionService, never())
                .deleteSessionsByUserID(any());
    }

    @Test
    public void Deleting_sessions_with_invalid_user_id_is_not_successful() throws Exception {
        String expectedUserID = Instancio.create(String.class);

        doThrow(UserSessionsNotFoundException.class)
                .when(sessionService).deleteSessionsByUserID(eq(expectedUserID));

        mvc.perform(delete("/sessions/by-user-id")
                        .param("user-id", expectedUserID))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_JSON));
    }

    @Test
    public void Deleting_session_is_successful() throws Exception {
        SessionRefreshingData expectedRefreshingData = Instancio.create(SessionRefreshingData.class);

        mvc.perform(post("/sessions/session-removing-process")
                    .content(objectMapper.writeValueAsString(expectedRefreshingData))
                    .contentType(APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful());

        verify(sessionService)
                .deleteSession(eq(expectedRefreshingData.refreshToken()));
    }

    @Test
    public void Deleting_session_with_invalid_content_type_is_not_successful() throws Exception {
        SessionRefreshingData expectedRefreshingData = Instancio.create(SessionRefreshingData.class);

        mvc.perform(post("/sessions/session-removing-process")
                        .content(objectMapper.writeValueAsString(expectedRefreshingData)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON));

        verify(sessionService, never())
                .deleteSession(any());
    }

    @Test
    public void Deleting_session_with_blank_data_is_not_successful() throws Exception {
        SessionRefreshingData refreshingData = new SessionRefreshingData("");

        mvc.perform(post("/sessions/session-removing-process")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshingData)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON));

        verify(sessionService, never())
                .deleteSession(any());
    }

    @Test
    public void Deleting_session_with_nullable_data_is_not_successful() throws Exception {
        SessionRefreshingData refreshingData = new SessionRefreshingData(null);

        mvc.perform(post("/sessions/session-removing-process")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshingData)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON));

        verify(sessionService, never())
                .deleteSession(any());
    }

    @Test
    public void Deleting_session_with_invalid_token_is_not_successful() throws Exception {
        SessionRefreshingData refreshingData = Instancio.create(SessionRefreshingData.class);

        doThrow(InvalidTokenException.class)
                .when(sessionService).deleteSession(eq(refreshingData.refreshToken()));

        mvc.perform(post("/sessions/session-removing-process")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshingData)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON));
    }
}
package com.rednet.sessionservice.controller;

import com.rednet.sessionservice.entity.Session;
import com.rednet.sessionservice.payload.request.CreateSessionRequestBody;
import com.rednet.sessionservice.payload.request.DeleteSessionRequestBody;
import com.rednet.sessionservice.payload.request.RefreshSessionRequestBody;
import com.rednet.sessionservice.payload.response.SimpleResponseBody;
import com.rednet.sessionservice.service.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController()
@RequestMapping(produces = APPLICATION_JSON_VALUE)
public class SessionController {
    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping(path = "/sessions", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Session> createSession(@RequestBody CreateSessionRequestBody requestBody) {
        return ResponseEntity.ok(sessionService.createSession(requestBody.userID(),requestBody.roles()));
    }

    @GetMapping("/sessions/{session-id}")
    public ResponseEntity<Session> getSession(@PathVariable("session-id") String sessionID) {
        return ResponseEntity.ok(sessionService.getSession(sessionID));
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<Session>> getSessionsByUserID(@RequestParam("user-id") String userID) {
        return ResponseEntity.ok(sessionService.getSessionsByUserID(userID));
    }

    @PutMapping("/sessions")
    public ResponseEntity<Session> refreshSession(@RequestBody RefreshSessionRequestBody requestBody) {
        return ResponseEntity.ok(sessionService.refreshSession(requestBody.refreshToken()));
    }

    @PostMapping(path = "session-removing-process", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<SimpleResponseBody> deleteSession(@RequestBody DeleteSessionRequestBody requestBody) {
        sessionService.deleteSession(requestBody.refreshToken());
        return ResponseEntity.ok(new SimpleResponseBody("session successfully deleted"));
    }

    @DeleteMapping("/sessions")
    public ResponseEntity<SimpleResponseBody> deleteSessionsByUserID(@RequestParam("user-id") String userID) {
        sessionService.deleteSessionsByUserID(userID);
        return ResponseEntity.ok(new SimpleResponseBody("sessions successfully deleted"));
    }
}

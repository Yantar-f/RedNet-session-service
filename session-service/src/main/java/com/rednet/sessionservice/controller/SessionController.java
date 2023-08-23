package com.rednet.sessionservice.controller;

import com.rednet.sessionservice.entity.Session;
import com.rednet.sessionservice.payload.request.CreateSessionRequestBody;
import com.rednet.sessionservice.payload.request.DeleteSessionRequestBody;
import com.rednet.sessionservice.payload.response.SimpleResponseBody;
import com.rednet.sessionservice.service.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(produces = APPLICATION_JSON_VALUE)
public class SessionController {
    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping(path = "/create-session", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Session> createSession(@RequestBody CreateSessionRequestBody requestBody) {
        return ResponseEntity.ok(sessionService.createSession(requestBody));
    }

    @GetMapping("/get-session")
    public ResponseEntity<Session> getSession(@RequestParam("session-id") String sessionID) {
        return ResponseEntity.ok(sessionService.getSession(sessionID));
    }

    @GetMapping("/get-sessions-by-user-id")
    public ResponseEntity<List<Session>> getSessionsByUserID(@RequestParam("user-id") String userID) {
        return ResponseEntity.ok(sessionService.getSessionsByUserID(userID));
    }

    @PostMapping(path = "delete-session", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<SimpleResponseBody> deleteSession(@RequestBody DeleteSessionRequestBody requestBody) {
        sessionService.deleteSession(requestBody.refreshToken());
        return ResponseEntity.ok(new SimpleResponseBody("session successfully deleted"));
    }

    @PostMapping("/delete-sessions-by-user-id")
    public ResponseEntity<SimpleResponseBody> deleteSessionsByUserID(@RequestParam("user-id") String userID) {
        sessionService.deleteSessionsByUserID(userID);
        return ResponseEntity.ok(new SimpleResponseBody("sessions successfully deleted"));
    }
}

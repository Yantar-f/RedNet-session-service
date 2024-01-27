package com.rednet.sessionservice.controller;

import com.rednet.sessionservice.entity.Session;
import com.rednet.sessionservice.model.SessionCreationData;
import com.rednet.sessionservice.model.SessionRefreshingData;
import com.rednet.sessionservice.service.SessionService;
import jakarta.validation.Valid;
import org.hibernate.validator.constraints.Length;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@Validated
@RequestMapping("/sessions")
public class SessionController {
    private SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Session> createSession(@Valid @RequestBody SessionCreationData creationData) {
        return ResponseEntity.ok(sessionService.createSession(creationData));
    }

    @GetMapping(path = "/by-id", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Session> getSession(
            @RequestParam("id")
            @Length(min = 1, message = "SessionID min length is 1")
            String sessionID) {

        return ResponseEntity.ok(sessionService.getSession(sessionID));
    }

    @GetMapping(path = "/by-user-id", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Session>> getSessionsByUserID(
            @RequestParam("user-id")
            @Length(min = 1, message = "UserID min length is 1")
            String userID) {

        return ResponseEntity.ok(sessionService.getSessionsByUserID(userID));
    }

    @PutMapping(produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Session> refreshSession(@Valid @RequestBody SessionRefreshingData requestBody) {
        return ResponseEntity.ok(sessionService.refreshSession(requestBody.refreshToken()));
    }

    @DeleteMapping("/by-user-id")
    public ResponseEntity<Void> deleteSessionsByUserID(
            @RequestParam("user-id")
            @Length(min = 1, message = "UserID min length is 1")
            String userID) {

        sessionService.deleteSessionsByUserID(userID);
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/session-removing-process", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteSession(@Valid @RequestBody SessionRefreshingData requestBody) {
        sessionService.deleteSession(requestBody.refreshToken());
        return ResponseEntity.ok().build();
    }
}

package com.rednet.sessionservice.repository;

import com.rednet.sessionservice.entity.Session;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
public interface SessionRepository {
    Mono<Session> createSession(String userID, String[] roles);
    Mono<Session> getSession(String sessionID);
    Flux<Session> getSessionsByUserID(String userID);
    Mono<Optional<Session>> refreshSession(String sessionID);
}

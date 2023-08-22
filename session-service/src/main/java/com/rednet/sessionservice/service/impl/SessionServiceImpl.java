package com.rednet.sessionservice.service.impl;

import com.rednet.sessionservice.repository.SessionRepository;
import com.rednet.sessionservice.service.SessionService;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Service
public class SessionServiceImpl implements SessionService {
    private final SessionRepository sessionRepository;

    public SessionServiceImpl(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    public Mono<ServerResponse> createSession(ServerRequest request) {

        return null;
    }

    @Override
    public Mono<ServerResponse> getSession(ServerRequest request) {
        return null;
    }

    @Override
    public Mono<ServerResponse> getSessionsByUserID(ServerRequest request) {
        return null;
    }

    @Override
    public Mono<ServerResponse> refreshSession(ServerRequest request) {
        return null;
    }

    @Override
    public Mono<ServerResponse> deleteSession(ServerRequest request) {
        return null;
    }

    @Override
    public Mono<ServerResponse> deleteSessionsByUserID(ServerRequest request) {
        return null;
    }
}

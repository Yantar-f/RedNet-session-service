package com.rednet.sessionservice.service;

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public interface SessionService {
    Mono<ServerResponse> createSession(ServerRequest request);
    Mono<ServerResponse> getSession(ServerRequest request);
    Mono<ServerResponse> getSessionsByUserID(ServerRequest request);
    Mono<ServerResponse> refreshSession(ServerRequest request);
    Mono<ServerResponse> deleteSession(ServerRequest request);
    Mono<ServerResponse> deleteSessionsByUserID(ServerRequest request);
}

package com.rednet.sessionservice.repository.impl;

import com.rednet.sessionservice.entity.Session;
import com.rednet.sessionservice.repository.SessionRepository;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
public class SessionRepositoryImpl implements SessionRepository {

    @Override
    public Mono<Session> createSession(String userID, String[] roles) {
        return null;
    }

    @Override
    public Mono<Session> getSession(String sessionID) {
        return null;
    }

    @Override
    public Flux<Session> getSessionsByUserID(String userID) {
        return null;
    }

    @Override
    public Mono<Optional<Session>> refreshSession(String sessionID) {
        return null;
    }
}

package com.rednet.sessionservice.repository.impl;

import com.rednet.sessionservice.entity.Session;
import com.rednet.sessionservice.model.SessionID;
import com.rednet.sessionservice.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.InsertOptions;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.springframework.data.cassandra.core.query.Criteria.where;
import static org.springframework.data.cassandra.core.query.Query.query;

@Repository
public class SessionRepositoryImpl implements SessionRepository {
    private final CassandraOperations operations;
    private final InsertOptions insertOptions;

    public SessionRepositoryImpl(
        CassandraOperations operations,
        @Value("${rednet.app.security.refresh-token.expiration-ms}") long refreshTokenExpirationMs
    ) {
        this.operations = operations;

        this.insertOptions = InsertOptions.builder()
            .ttl((int) MILLISECONDS.toSeconds(refreshTokenExpirationMs) + 10)
            .build();
    }

    @Override
    public Optional<Session> findByID(SessionID sessionID) {
        return findByID(sessionID.getUserID(), sessionID.getSessionKey());
    }

    @Override
    public List<Session> findAllByUserID(String userID) {
        return operations.select(query(where("user_id").is(userID)), Session.class);
    }

    @Override
    public Session insert(Session session) {
        return operations.insert(session, insertOptions).getEntity();
    }

    @Override
    public boolean deleteByID(SessionID sessionID) {
        return operations.delete(query(List.of(
            where("user_id").is(sessionID.getUserID()),
            where("session_key").is(sessionID.getSessionKey())
        )), Session.class);
    }

    @Override
    public boolean deleteAllByUserID(String userID) {
        return operations.delete(query(where("user_id").is(userID)), Session.class);
    }

    @Override
    public boolean existsByUserID(String userID) {
        return operations.exists(query(where("user_id").is(userID)), Session.class);
    }

    private Optional<Session> findByID(String userID, String sessionID) {
        return Optional.ofNullable(operations.selectOne(query(List.of(
                where("user_id").is(userID),
                where("session_key").is(sessionID)
        )).limit(1), Session.class));
    }
}

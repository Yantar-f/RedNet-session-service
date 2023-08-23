package com.rednet.sessionservice.repository;

import com.rednet.sessionservice.entity.Session;
import com.rednet.sessionservice.entity.SessionKey;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionRepository extends CassandraRepository<Session, SessionKey> {
    List<Session> findAllBySessionKey_UserID(String userID);
    void deleteAllBySessionKey_UserID(String userID);
}

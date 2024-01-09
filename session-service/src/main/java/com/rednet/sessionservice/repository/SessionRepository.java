package com.rednet.sessionservice.repository;

import com.rednet.sessionservice.entity.Session;
import com.rednet.sessionservice.model.SessionID;

import java.util.List;
import java.util.Optional;

public interface SessionRepository {
    Optional<Session> findByID          (SessionID sessionID);
    List<Session>     findAllByUserID   (String userID);
    Session insert              (Session session);
    boolean deleteByID          (SessionID sessionID);
    boolean deleteAllByUserID   (String userID);
    boolean existsByUserID      (String userID);
}

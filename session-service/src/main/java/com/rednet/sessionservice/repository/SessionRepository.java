package com.rednet.sessionservice.repository;

import com.rednet.sessionservice.entity.Session;

import java.util.List;
import java.util.Optional;

public interface SessionRepository {
    Optional<Session> findByID(String userID, String sessionPostfix);
    List<Session> findAllByUserID(String userID);
    Session insert(Session session);
    boolean deleteByID(String userID, String sessionPostfix);
    boolean deleteAllByUserID(String userID);
    boolean existsByUserID(String userID);
}

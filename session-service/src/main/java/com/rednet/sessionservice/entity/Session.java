package com.rednet.sessionservice.entity;

import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.io.Serializable;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.springframework.data.cassandra.core.cql.Ordering.DESCENDING;
import static org.springframework.data.cassandra.core.cql.PrimaryKeyType.CLUSTERED;
import static org.springframework.data.cassandra.core.cql.PrimaryKeyType.PARTITIONED;
import static org.springframework.data.cassandra.core.mapping.CassandraType.Name.LIST;
import static org.springframework.data.cassandra.core.mapping.CassandraType.Name.TEXT;
import static org.springframework.data.cassandra.core.mapping.CassandraType.Name.TIMESTAMP;

@Table("sessions")
public class Session implements Serializable {
    @PrimaryKeyColumn(name = "user_id", type = PARTITIONED)
    private String userID;

    @PrimaryKeyColumn(name = "session_key", type = CLUSTERED, ordinal = 0)
    private String sessionKey;

    @PrimaryKeyColumn(name = "created_at", type = CLUSTERED, ordering = DESCENDING, ordinal = 1)
    @CassandraType(type = TIMESTAMP)
    private Instant refreshedAt;

    @Column("createdAt")
    @CassandraType(type = TIMESTAMP)
    private Instant createdAt;

    @Column
    @CassandraType(typeArguments = TEXT, type = LIST)
    private String[] roles;

    @Column("access_token")
    private String accessToken;

    @Column("refresh_token")
    private String refreshToken;

    @Column("token_id")
    private String tokenID;

    protected Session() {}

    public Session(String userID,
                   String sessionKey,
                   Instant createdAt,
                   String[] roles,
                   String accessToken,
                   String refreshToken,
                   String tokenID) {

        this.userID = userID;
        this.sessionKey = sessionKey;
        this.refreshedAt = createdAt;
        this.createdAt = createdAt;
        this.roles = roles;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenID = tokenID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public String[] getRoles() {
        return roles;
    }

    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getTokenID() {
        return tokenID;
    }

    public void setTokenID(String tokenID) {
        this.tokenID = tokenID;
    }

    public Instant getRefreshedAt() {
        return refreshedAt;
    }

    public void setRefreshedAt(Instant refreshedAt) {
        this.refreshedAt = refreshedAt;
    }

    @Override
    public int hashCode() {
        return  userID.hashCode() *
                sessionKey.hashCode() *
                createdAt.hashCode() *
                refreshedAt.hashCode() *
                Arrays.hashCode(roles) *
                accessToken.hashCode() *
                refreshToken.hashCode() *
                tokenID.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (! (obj instanceof Session session)) return false;

        return  userID.equals(session.userID) &&
                sessionKey.equals(session.sessionKey) &&
                createdAt.equals(session.createdAt) &&
                refreshedAt.equals(session.refreshedAt) &&
                accessToken.equals(session.accessToken) &&
                refreshToken.equals(session.refreshToken) &&
                tokenID.equals(session.tokenID) &&
                roles.length == session.roles.length &&
                new HashSet<>(List.of(roles)).containsAll(List.of(session.roles)) &&
                new HashSet<>(List.of(session.roles)).containsAll(List.of(roles));
    }

    @Override
    public String toString() {
        return "{" +
                "\n\tuserID: " + userID +
                "\n\tsessionKey: " + sessionKey +
                "\n\tcreatedAt: " + createdAt +
                "\n\trefreshedAt: " + refreshedAt +
                "\n\troles: " + Arrays.toString(roles) +
                "\n\taccessToken: " + accessToken +
                "\n\trefreshToken: " + refreshToken +
                "\n\ttokenID: " + tokenID +
                "\n}";
    }

    public boolean isTheSameAs(Session other) {
        return  userID.equals(other.userID) &&
                sessionKey.equals(other.sessionKey) &&
                createdAt.equals(other.createdAt);
    }

    public boolean isNewerThen(Session other) {
        return refreshedAt.isAfter(other.refreshedAt);
    }
}

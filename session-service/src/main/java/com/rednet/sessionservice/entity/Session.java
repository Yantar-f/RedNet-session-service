package com.rednet.sessionservice.entity;


import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.io.Serializable;
import java.util.Date;

import static org.springframework.data.cassandra.core.cql.Ordering.DESCENDING;
import static org.springframework.data.cassandra.core.cql.PrimaryKeyType.CLUSTERED;
import static org.springframework.data.cassandra.core.cql.PrimaryKeyType.PARTITIONED;
import static org.springframework.data.cassandra.core.mapping.CassandraType.Name.LIST;
import static org.springframework.data.cassandra.core.mapping.CassandraType.Name.TEXT;

@Table("sessions")
public class Session implements Serializable {
    @PrimaryKeyColumn(name = "user_id", type = PARTITIONED)
    private String userID;

    @PrimaryKeyColumn(name = "session_postfix", type = CLUSTERED, ordinal = 0)
    private String sessionPostfix;

    @PrimaryKeyColumn(name = "created_at", type = CLUSTERED, ordering = DESCENDING, ordinal = 1)
    private Date createdAt;

    @Column
    @CassandraType(typeArguments = TEXT , type = LIST)
    private String[] roles;

    @Column("access_token")
    private String accessToken;

    @Column("refresh_token")
    private String refreshToken;

    @Column("token_id")
    private String tokenID;

    public Session() {

    }

    public Session(
        String userID,
        String sessionPostfix,
        Date createdAt, String[] roles,
        String accessToken,
        String refreshToken,
        String tokenID
    ) {
        this.userID = userID;
        this.sessionPostfix = sessionPostfix;
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

    public String getSessionPostfix() {
        return sessionPostfix;
    }

    public void setSessionPostfix(String sessionPostfix) {
        this.sessionPostfix = sessionPostfix;
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

    public String getTokenID() {
        return tokenID;
    }

    public void setTokenID(String tokenID) {
        this.tokenID = tokenID;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}

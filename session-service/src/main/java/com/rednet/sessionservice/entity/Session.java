package com.rednet.sessionservice.entity;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.io.Serializable;

@Table("sessions")
public class Session implements Serializable {
    @PrimaryKey
    private SessionKey sessionKey;

    @Column
    private String[] roles;

    @Column("access_token")
    private String accessToken;

    @Column("refresh_token")
    private String refreshToken;

    @Column("token_id")
    private String tokenID;

    private Session() {}
    public Session(SessionKey sessionKey, String[] roles, String accessToken, String refreshToken, String tokenID) {
        this.sessionKey = sessionKey;
        this.roles = roles;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenID = tokenID;
    }

    public SessionKey getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(SessionKey sessionKey) {
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

    public String getTokenID() {
        return tokenID;
    }

    public void setTokenID(String tokenID) {
        this.tokenID = tokenID;
    }
}

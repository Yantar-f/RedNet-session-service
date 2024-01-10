package com.rednet.sessionservice.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class TokenClaims {
    private String      subjectID;
    private String      sessionID;
    private String      tokenID;
    private String[]    roles;

    public TokenClaims(String subjectID, String sessionID, String tokenID, String[] roles) {
        this.subjectID  = subjectID;
        this.sessionID  = sessionID;
        this.tokenID    = tokenID;
        this.roles      = roles;
    }

    public String getSubjectID() {
        return subjectID;
    }

    public void setSubjectID(String subjectID) {
        this.subjectID = subjectID;
    }

    public String getSessionID() {
        return sessionID;
    }

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    public String getTokenID() {
        return tokenID;
    }

    public void setTokenID(String tokenID) {
        this.tokenID = tokenID;
    }

    public String[] getRoles() {
        return roles;
    }

    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    @Override
    public int hashCode() {
        return subjectID.hashCode() * sessionID.hashCode() * tokenID.hashCode() * Arrays.hashCode(roles);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;

        TokenClaims tokenClaims = (TokenClaims) obj;

        return  subjectID.equals(tokenClaims.subjectID) &&
                sessionID.equals(tokenClaims.sessionID) &&
                tokenID.equals(tokenClaims.tokenID) &&
                roles.length == tokenClaims.roles.length &&
                new HashSet<>(List.of(roles)).containsAll(List.of(tokenClaims.roles)) &&
                new HashSet<>(List.of(tokenClaims.roles)).containsAll(List.of(roles));
    }
}

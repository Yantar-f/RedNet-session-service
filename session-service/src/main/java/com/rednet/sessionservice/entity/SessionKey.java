package com.rednet.sessionservice.entity;

import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import static org.springframework.data.cassandra.core.cql.PrimaryKeyType.PARTITIONED;

@PrimaryKeyClass
public class SessionKey {
    @PrimaryKeyColumn(name = "user_id", type = PARTITIONED)
    private String userID;

    @PrimaryKeyColumn(name = "session_postfix", type = PARTITIONED)
    private String sessionPostfix;


    public SessionKey(String userID, String sessionPostfix) {
        this.userID = userID;
        this.sessionPostfix = sessionPostfix;
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
}

package com.rednet.sessionservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ApiTokenConfig extends TokenConfig {
    protected ApiTokenConfig(
            @Value("${rednet.app.security.api-token.issuer}") String issuer,
            @Value("${rednet.app.security.api-token.activation-ms}") long activationMs,
            @Value("${rednet.app.security.api-token.expiration-ms}") long expirationMs,
            @Value("${rednet.app.security.api-token.allowed-clock-skew-s}") long allowedClockSkew,
            @Value("${rednet.app.security.api-token.cookie-name}") String cookieName,
            @Value("${rednet.app.security.api-token.cookie-path}") String cookiePath,
            @Value("${rednet.app.security.api-token.cookie-expiration-s}") long cookieExpirationS,
            @Value("${rednet.app.security.api-token.secret-key}") String secretKey) {
        super(
                issuer,
                activationMs,
                expirationMs,
                allowedClockSkew,
                cookieName,
                cookiePath,
                cookieExpirationS,
                secretKey
        );
    }

    @Override
    public String getTokenTypeName() {
        return "api token";
    }
}

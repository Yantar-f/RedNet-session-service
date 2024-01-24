package com.rednet.sessionservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AccessTokenConfig extends TokenConfig {
    protected AccessTokenConfig(
            @Value("${rednet.app.security.access-token.issuer}") String issuer,
            @Value("${rednet.app.security.access-token.activation-ms}") long activationMs,
            @Value("${rednet.app.security.access-token.expiration-ms}") long expirationMs,
            @Value("${rednet.app.security.access-token.allowed-clock-skew-s}") long allowedClockSkew,
            @Value("${rednet.app.security.access-token.cookie-name}") String cookieName,
            @Value("${rednet.app.security.access-token.cookie-path}") String cookiePath,
            @Value("${rednet.app.security.access-token.cookie-expiration-s}") long cookieExpirationS,
            @Value("${rednet.app.security.access-token.secret-key}") String secretKey) {
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
        return "access token";
    }
}

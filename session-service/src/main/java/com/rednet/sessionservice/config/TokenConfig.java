package com.rednet.sessionservice.config;

public abstract class TokenConfig {
    private final String issuer;
    private final long activationMs;
    private final long expirationMs;
    private final long allowedClockSkew;
    private final String cookieName;
    private final String cookiePath;
    private final long cookieExpirationS;
    private final String secretKey;

    protected TokenConfig(String issuer,
                          long activationMs,
                          long expirationMs,
                          long allowedClockSkew,
                          String cookieName,
                          String cookiePath,
                          long cookieExpirationS,
                          String secretKey) {
        this.issuer = issuer;
        this.activationMs = activationMs;
        this.expirationMs = expirationMs;
        this.allowedClockSkew = allowedClockSkew;
        this.cookieName = cookieName;
        this.cookiePath = cookiePath;
        this.cookieExpirationS = cookieExpirationS;
        this.secretKey = secretKey;
    }

    public String getIssuer() {
        return issuer;
    }

    public long getActivationMs() {
        return activationMs;
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public long getAllowedClockSkew() {
        return allowedClockSkew;
    }

    public String getCookieName() {
        return cookieName;
    }

    public String getCookiePath() {
        return cookiePath;
    }

    public long getCookieExpirationS() {
        return cookieExpirationS;
    }

    public abstract String getTokenTypeName();

    public String getSecretKey() {
        return secretKey;
    }
}

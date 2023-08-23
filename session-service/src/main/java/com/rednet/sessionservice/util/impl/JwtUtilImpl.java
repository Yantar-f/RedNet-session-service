package com.rednet.sessionservice.util.impl;

import com.rednet.sessionservice.util.JwtUtil;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

import static io.jsonwebtoken.SignatureAlgorithm.HS256;
import static io.jsonwebtoken.io.Decoders.BASE64;

@Component
public class JwtUtilImpl implements JwtUtil {
    private final String authTokenIssuer;
    private final String accessTokenSecretKey;
    private final long accessTokenExpirationMs;
    private final String refreshTokenSecretKey;
    private final long refreshTokenExpirationMs;
    private final JwtParser accessTokenParser;
    private final JwtParser apiTokenParser;
    private final JwtParser refreshTokenParser;

    public JwtUtilImpl(
        @Value("${rednet.app.auth-token-issuer}") String authTokenIssuer,
        @Value("${rednet.app.access-token-secret-key}") String accessTokenSecretKey,
        @Value("${rednet.app.access-token-expiration-ms}") long accessTokenExpirationMs,
        @Value("${rednet.app.refresh-token-secret-key}") String refreshTokenSecretKey,
        @Value("${rednet.app.refresh-token-expiration-ms}") long refreshTokenExpirationMs,
        @Value("${rednet.app.api-token-secret-key}") String apiTokenSecretKey,
        @Value("${rednet.app.api-token-issuer}") String apiTokenIssuer
    ) {
        this.authTokenIssuer = authTokenIssuer;
        this.accessTokenSecretKey = accessTokenSecretKey;
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenSecretKey = refreshTokenSecretKey;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
        this.accessTokenParser = Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(BASE64.decode(accessTokenSecretKey)))
            .requireIssuer(authTokenIssuer)
            .setAllowedClockSkewSeconds(5)
            .build();
        this.apiTokenParser = Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(BASE64.decode(apiTokenSecretKey)))
            .requireIssuer(apiTokenIssuer)
            .setAllowedClockSkewSeconds(5)
            .build();
        this.refreshTokenParser = Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(BASE64.decode(refreshTokenSecretKey)))
            .requireIssuer(authTokenIssuer)
            .setAllowedClockSkewSeconds(5)
            .build();
    }

    @Override
    public JwtBuilder generateAccessTokenBuilder() {
        return Jwts.builder()
            .setIssuer(authTokenIssuer)
            .signWith(Keys.hmacShaKeyFor(BASE64.decode(accessTokenSecretKey)), HS256)
            .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpirationMs));
    }

    @Override
    public JwtBuilder generateRefreshTokenBuilder() {
        return Jwts.builder()
            .setIssuer(authTokenIssuer)
            .signWith(Keys.hmacShaKeyFor(BASE64.decode(refreshTokenSecretKey)), HS256)
            .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpirationMs));
    }

    @Override
    public JwtParser getAccessTokenParser() {
        return accessTokenParser;
    }

    @Override
    public JwtParser getApiTokenParser() {
        return apiTokenParser;
    }

    @Override
    public JwtParser getRefreshTokenParser() {
        return refreshTokenParser;
    }
}

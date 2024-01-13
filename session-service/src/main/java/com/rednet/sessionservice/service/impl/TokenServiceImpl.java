package com.rednet.sessionservice.service.impl;

import com.rednet.sessionservice.exception.impl.InvalidTokenException;
import com.rednet.sessionservice.model.TokenClaims;
import com.rednet.sessionservice.service.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static io.jsonwebtoken.SignatureAlgorithm.HS256;
import static io.jsonwebtoken.io.Decoders.BASE64;

@Service
public class TokenServiceImpl implements TokenService {
    private final String    accessTokenIssuer;
    private final String    accessTokenSecretKey;
    private final long      accessTokenExpirationMs;
    private final String    refreshTokenSecretKey;
    private final long      refreshTokenExpirationMs;
    private final String    refreshTokenIssuer;
    private final JwtParser accessTokenParser;
    private final JwtParser apiTokenParser;
    private final JwtParser refreshTokenParser;

    public TokenServiceImpl(
            @Value("${rednet.app.security.access-token.issuer}") String                 accessTokenIssuer,
            @Value("${rednet.app.security.access-token.secret-key}") String             accessTokenSecretKey,
            @Value("${rednet.app.security.access-token.expiration-ms}") long            accessTokenExpirationMs,
            @Value("${rednet.app.security.access-token.allowed-clock-skew-s}") long     accessTokenAllowedClockSkewS,
            @Value("${rednet.app.security.refresh-token.issuer}") String                refreshTokenIssuer,
            @Value("${rednet.app.security.refresh-token.secret-key}") String            refreshTokenSecretKey,
            @Value("${rednet.app.security.refresh-token.expiration-ms}") long           refreshTokenExpirationMs,
            @Value("${rednet.app.security.refresh-token.allowed-clock-skew-s}") long    refreshTokenAllowedClockSkewS,
            @Value("${rednet.app.security.api-token.secret-key}") String                apiTokenSecretKey,
            @Value("${rednet.app.security.api-token.issuer}") String                    apiTokenIssuer,
            @Value("${rednet.app.security.api-token.allowed-clock-skew-s}") long        apiTokenAllowedClockSkewS
    ) {
        this.accessTokenIssuer = accessTokenIssuer;
        this.accessTokenSecretKey = accessTokenSecretKey;
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenSecretKey = refreshTokenSecretKey;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
        this.refreshTokenIssuer = refreshTokenIssuer;

        accessTokenParser = buildParser(accessTokenSecretKey, accessTokenIssuer, accessTokenAllowedClockSkewS);
        apiTokenParser = buildParser(apiTokenSecretKey, apiTokenIssuer, apiTokenAllowedClockSkewS);
        refreshTokenParser = buildParser(refreshTokenSecretKey, refreshTokenIssuer, refreshTokenAllowedClockSkewS);
    }

    @Override
    public String generateAccessToken(TokenClaims claims) {
        return  generateToken(claims, accessTokenSecretKey, accessTokenExpirationMs, accessTokenIssuer);
    }

    @Override
    public String generateRefreshToken(TokenClaims claims) {
        return generateToken(claims, refreshTokenSecretKey, refreshTokenExpirationMs, refreshTokenIssuer);
    }

    @Override
    public TokenClaims parseAccessToken(String token) {
        return parseWith(accessTokenParser, token);
    }

    @Override
    public TokenClaims parseRefreshToken(String token) {
        return parseWith(refreshTokenParser, token);
    }

    @Override
    public TokenClaims parseApiToken(String token) {
        return parseWith(apiTokenParser, token);
    }

    private TokenClaims parseWith(JwtParser parser, String token) {
        try {
            Claims  claimsMap = parser.parseClaimsJws(token).getBody();
            List<?> convertedRoles = claimsMap.get("roles", ArrayList.class);

            return new TokenClaims(
                    claimsMap.getSubject(),
                    claimsMap.get("sid", String.class),
                    claimsMap.getId(),
                    convertedRoles
                            .stream()
                            .map(obj -> (String) obj)
                            .toArray(String[]::new)
            );
        } catch (
                SignatureException |
                MalformedJwtException |
                ExpiredJwtException |
                UnsupportedJwtException |
                IllegalArgumentException |
                ClassCastException e
        ) {
            throw new InvalidTokenException(token);
        }
    }

    private String generateToken(TokenClaims claims, String secretKey, long expirationMs, String issuer) {
        return Jwts.builder()
                .signWith(Keys.hmacShaKeyFor(BASE64.decode(secretKey)), HS256)
                .setIssuer(issuer)
                .setExpiration(Date.from(Instant.now().plusMillis(expirationMs)))
                .setId(claims.getTokenID())
                .setSubject(claims.getSubjectID())
                .claim("roles", claims.getRoles())
                .claim("sid", claims.getSessionID())
                .compact();
    }

    private JwtParser buildParser(String secretKey, String issuer, long allowedClockSkewS) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(BASE64.decode(secretKey)))
                .requireIssuer(issuer)
                .setAllowedClockSkewSeconds(allowedClockSkewS)
                .build();
    }
}

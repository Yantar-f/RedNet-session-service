package com.rednet.sessionservice.util.impl;

import com.rednet.sessionservice.config.AccessTokenConfig;
import com.rednet.sessionservice.config.ApiTokenConfig;
import com.rednet.sessionservice.config.RefreshTokenConfig;
import com.rednet.sessionservice.config.TokenConfig;
import com.rednet.sessionservice.exception.impl.InvalidTokenException;
import com.rednet.sessionservice.model.TokenClaims;
import com.rednet.sessionservice.util.TokenUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static io.jsonwebtoken.SignatureAlgorithm.HS256;
import static io.jsonwebtoken.io.Decoders.BASE64;

@Service
public class TokenUtilImpl implements TokenUtil {
    private final AccessTokenConfig accessTokenConfig;
    private final RefreshTokenConfig refreshTokenConfig;
    private final JwtParser accessTokenParser;
    private final JwtParser apiTokenParser;
    private final JwtParser refreshTokenParser;

    public TokenUtilImpl(AccessTokenConfig accessTokenConfig,
                         ApiTokenConfig apiTokenConfig,
                         RefreshTokenConfig refreshTokenConfig) {
        this.accessTokenConfig = accessTokenConfig;
        this.refreshTokenConfig = refreshTokenConfig;

        accessTokenParser = buildParser(accessTokenConfig);
        apiTokenParser = buildParser(apiTokenConfig);
        refreshTokenParser = buildParser(refreshTokenConfig);
    }

    @Override
    public String generateAccessToken(TokenClaims claims) {
        return generateToken(claims, accessTokenConfig);
    }

    @Override
    public String generateRefreshToken(TokenClaims claims) {
        return generateToken(claims, refreshTokenConfig);
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
            Claims claimsMap = extractClaimsMapWith(parser, token);
            String subjectID = extractSubjectIDFrom(claimsMap);
            String sessionID = extractSessionIDFrom(claimsMap);
            String tokenID = extractTokenIDFrom(claimsMap);
            String[] roles = extractRolesFrom(claimsMap);

            return new TokenClaims(subjectID, sessionID, tokenID, roles);
        } catch (SignatureException |
                 MalformedJwtException |
                 ExpiredJwtException |
                 UnsupportedJwtException |
                 IllegalArgumentException |
                 ClassCastException exception) {
            throw new InvalidTokenException(token);
        }
    }

    private Claims extractClaimsMapWith(JwtParser parser, String token) {
        return parser.parseClaimsJws(token).getBody();
    }

    private String extractSubjectIDFrom(Claims claimsMap) {
        return claimsMap.getSubject();
    }

    private String extractSessionIDFrom(Claims claimsMap) {
        return claimsMap.get("sid", String.class);
    }

    private String extractTokenIDFrom(Claims claimsMap) {
        return claimsMap.getId();
    }

    private String[] extractRolesFrom(Claims claimsMap) {
        List<?> convertedRoles = claimsMap.get("roles", ArrayList.class);
        return convertedRoles.stream().map(String::valueOf).toArray(String[]::new);
    }

    private String generateToken(TokenClaims claims, TokenConfig config) {
        return Jwts.builder()
                .signWith(Keys.hmacShaKeyFor(BASE64.decode(config.getSecretKey())), HS256)
                .setIssuer(config.getIssuer())
                .setExpiration(Date.from(Instant.now().plusMillis(config.getExpirationMs())))
                .setId(claims.getTokenID())
                .setSubject(claims.getSubjectID())
                .claim("roles", claims.getRoles())
                .claim("sid", claims.getSessionID())
                .compact();
    }

    private JwtParser buildParser(TokenConfig config) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(BASE64.decode(config.getSecretKey())))
                .requireIssuer(config.getIssuer())
                .setAllowedClockSkewSeconds(config.getAllowedClockSkew())
                .build();
    }
}

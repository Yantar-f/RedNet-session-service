package com.rednet.sessionservice.util;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import org.springframework.stereotype.Component;

@Component
public interface JwtUtil {
    JwtBuilder generateAccessTokenBuilder();
    JwtBuilder generateRefreshTokenBuilder();
    JwtParser getApiTokenParser();
    JwtParser getAccessTokenParser();
    JwtParser getRefreshTokenParser();
}

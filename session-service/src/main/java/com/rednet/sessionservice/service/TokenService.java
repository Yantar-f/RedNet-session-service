package com.rednet.sessionservice.service;

import com.rednet.sessionservice.model.TokenClaims;

public interface TokenService {
    String      generateAccessToken     (TokenClaims tokenClaims);
    String      generateRefreshToken    (TokenClaims tokenClaims);

    TokenClaims parseAccessToken    (String token);
    TokenClaims parseRefreshToken   (String token);
    TokenClaims parseApiToken       (String token);
}

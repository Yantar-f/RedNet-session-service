package com.rednet.sessionservice.service;

import com.rednet.sessionservice.model.TokenClaims;

public interface SessionTokenService {
    String      generateAccessToken     (TokenClaims tokenClaims);
    String      generateRefreshToken    (TokenClaims tokenClaims);
    TokenClaims parse                   (String token);
}

package com.rednet.sessionservice.service.impl;

import com.rednet.sessionservice.model.TokenClaims;
import com.rednet.sessionservice.service.SessionTokenService;

public class SessionTokenServiceImpl implements SessionTokenService {
    @Override
    public String generateAccessToken(TokenClaims tokenClaims) {
        return null;
    }

    @Override
    public String generateRefreshToken(TokenClaims tokenClaims) {
        return null;
    }

    @Override
    public TokenClaims parse(String token) {
        return null;
    }
}

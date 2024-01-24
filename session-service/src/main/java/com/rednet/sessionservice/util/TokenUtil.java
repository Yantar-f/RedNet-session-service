package com.rednet.sessionservice.util;

import com.rednet.sessionservice.model.TokenClaims;

public interface TokenUtil {
    String      generateAccessToken     (TokenClaims tokenClaims);
    String      generateRefreshToken    (TokenClaims tokenClaims);

    TokenClaims parseAccessToken    (String token);
    TokenClaims parseRefreshToken   (String token);
    TokenClaims parseApiToken       (String token);
}

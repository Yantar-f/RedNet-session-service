package com.rednet.sessionservice.filter;

import com.rednet.sessionservice.model.TokenClaims;
import com.rednet.sessionservice.service.TokenService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;
import java.util.Arrays;

@Component
public class ApiTokenFilter extends OncePerRequestFilter {
    private final String apiTokenCookieName;
    private final TokenService tokenService;

    public ApiTokenFilter(
        @Value("${rednet.app.security.api-token.cookie-name}") String apiTokenCookieName,
        TokenService tokenService
    ) {
        this.apiTokenCookieName = apiTokenCookieName;
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(
        @Nonnull HttpServletRequest     request,
        @Nonnull HttpServletResponse    response,
        @Nonnull FilterChain            filterChain
    ) throws ServletException, IOException {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            filterChain.doFilter(request,response);
            return;
        }

        Cookie apiTokenCookie = Arrays.stream(cookies)
            .filter(cookie -> cookie.getName().equals(apiTokenCookieName))
            .findFirst().orElse(null);

        if (apiTokenCookie == null) {
            filterChain.doFilter(request,response);
            return;
        }

        try {
            TokenClaims claims = tokenService.parseApiToken(apiTokenCookie.getValue());

            UsernamePasswordAuthenticationToken contextAuthToken =
                new UsernamePasswordAuthenticationToken(
                    claims.getSubjectID(),
                    apiTokenCookie.getValue(),
                    Arrays.stream(claims.getRoles())
                        .map(SimpleGrantedAuthority::new)
                        .toList()
                );

            contextAuthToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(contextAuthToken);
        } catch (
            SignatureException |
            MalformedJwtException |
            ExpiredJwtException |
            UnsupportedJwtException |
            IllegalArgumentException e
        ) {
            /*
            * LOG EVENT
             */
        }

        filterChain.doFilter(request,response);
    }
}


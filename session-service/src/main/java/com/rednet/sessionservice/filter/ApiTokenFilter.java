package com.rednet.sessionservice.filter;

import com.rednet.sessionservice.exception.impl.InvalidTokenException;
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
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class ApiTokenFilter extends OncePerRequestFilter {
    private final String apiTokenCookieName;
    private final TokenService tokenService;

    public ApiTokenFilter(@Value("${rednet.app.security.api-token.cookie-name}") String apiTokenCookieName,
                          TokenService tokenService) {
        this.apiTokenCookieName = apiTokenCookieName;
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain) throws ServletException, IOException {
        Optional<String> apiToken = extractApiTokenFromRequest(request);

        if (apiToken.isEmpty()) {
            filterChain.doFilter(request,response);
            return;
        }

        try {
            TokenClaims claims = tokenService.parseApiToken(apiToken.get());
            List<SimpleGrantedAuthority> authorities = convertRolesToAuthorities(claims.getRoles());

            UsernamePasswordAuthenticationToken contextAuthToken = new UsernamePasswordAuthenticationToken(
                    claims.getSubjectID(),
                    apiToken.get(),
                    authorities
            );

            contextAuthToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(contextAuthToken);
        } catch (InvalidTokenException exception) {
            /*
             * LOG EVENT
             */
        }

        filterChain.doFilter(request,response);
    }

    private Optional<String> extractApiTokenFromRequest(HttpServletRequest request) {
        Optional<Cookie> cookie = Optional.ofNullable(WebUtils.getCookie(request, apiTokenCookieName));
        return cookie.map(Cookie::getValue);
    }

    private List<SimpleGrantedAuthority> convertRolesToAuthorities(String[] roles) {
        return Arrays.stream(roles).map(SimpleGrantedAuthority::new).toList();
    }
}


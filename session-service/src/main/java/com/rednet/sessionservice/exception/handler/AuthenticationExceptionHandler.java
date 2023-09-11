package com.rednet.sessionservice.exception.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rednet.sessionservice.exception.ErrorResponseMessage;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.time.Instant;

@Component
public class AuthenticationExceptionHandler implements AuthenticationEntryPoint {
    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authException
    ) throws IOException, ServletException {
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        response.setStatus(status.value());

        new ObjectMapper().writeValue(
            response.getOutputStream(),
            new ErrorResponseMessage(
                status.name(),
                Instant.now(),
                request.getServletPath(),
                "Api authorization is required"
            )
        );
    }
}

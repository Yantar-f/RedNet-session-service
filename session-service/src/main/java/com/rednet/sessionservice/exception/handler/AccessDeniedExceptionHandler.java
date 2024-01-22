package com.rednet.sessionservice.exception.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rednet.sessionservice.exception.ErrorResponseMessage;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.time.Instant;

@Component
public class AccessDeniedExceptionHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        HttpStatus status = HttpStatus.FORBIDDEN;

        response.setStatus(status.value());

        new ObjectMapper().writeValue(
            response.getOutputStream(),
            new ErrorResponseMessage(
                status.name(),
                Instant.now(),
                request.getServletPath(),
                accessDeniedException.getMessage()
            )
        );
    }
}

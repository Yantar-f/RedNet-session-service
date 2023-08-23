package com.rednet.sessionservice.exception.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rednet.sessionservice.payload.response.ErrorResponseMessage;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

@Component
public class AuthenticationExceptionHandler implements AuthenticationEntryPoint {
    private final DateFormat dateFormat;

    AuthenticationExceptionHandler(DateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }

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
                dateFormat.format(new Date()),
                request.getServletPath(),
                new ArrayList<>(){{add("Api authorization is required");}}
            )
        );
    }
}

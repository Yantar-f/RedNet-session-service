package com.rednet.sessionservice.exception.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rednet.sessionservice.payload.response.ErrorResponseMessage;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

@Component
public class AccessDeniedExceptionHandler implements AccessDeniedHandler {
    private final DateFormat dateFormat;

    AccessDeniedExceptionHandler(DateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }

    @Override
    public void handle(
        HttpServletRequest request,
        HttpServletResponse response,
        AccessDeniedException accessDeniedException
    ) throws IOException, ServletException {
        HttpStatus status = HttpStatus.FORBIDDEN;

        response.setStatus(status.value());

        new ObjectMapper().writeValue(
            response.getOutputStream(),
            new ErrorResponseMessage(
                status.name(),
                dateFormat.format(new Date()),
                request.getServletPath(),
                new ArrayList<>(){{add(accessDeniedException.getMessage());}}
            )
        );
    }
}

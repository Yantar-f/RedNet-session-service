package com.rednet.sessionservice.exception.handler;

import com.rednet.sessionservice.exception.BadRequestException;
import com.rednet.sessionservice.payload.response.ErrorResponseMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private final DateFormat dateFormat;

    public GlobalExceptionHandler(DateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponseMessage> handleBadRequest(
        BadRequestException ex,
        HttpServletRequest request
    ) {
        return generateBadRequest(request.getServletPath(), ex.getMessages());
    }

    private ResponseEntity<ErrorResponseMessage> generateBadRequest(String path, List<String> errorMessages) {
        return ResponseEntity.badRequest().body(
            new ErrorResponseMessage(
                HttpStatus.BAD_REQUEST.name(),
                dateFormat.format(new Date()),
                path,
                errorMessages
            )
        );
    }
}

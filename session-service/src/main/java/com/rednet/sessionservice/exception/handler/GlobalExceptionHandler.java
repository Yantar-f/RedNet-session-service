package com.rednet.sessionservice.exception.handler;

import com.rednet.sessionservice.exception.BadRequestException;
import com.rednet.sessionservice.exception.NotFoundException;
import com.rednet.sessionservice.payload.response.ErrorResponseMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

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

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponseMessage> handleNotFound(
        NotFoundException ex,
        HttpServletRequest request
    ) {
        return generateNotFound(request.getServletPath(), ex.getMessages());
    }

    private ResponseEntity<ErrorResponseMessage> generateBadRequest(String path, List<String> errorMessages) {
        return ResponseEntity.badRequest().body(
            new ErrorResponseMessage(
                BAD_REQUEST.name(),
                dateFormat.format(new Date()),
                path,
                errorMessages
            )
        );
    }

    private ResponseEntity<ErrorResponseMessage> generateNotFound(String path, List<String> errorMessages) {
        return ResponseEntity.status(HttpStatusCode.valueOf(NOT_FOUND.value())).body(
            new ErrorResponseMessage(
                NOT_FOUND.name(),
                dateFormat.format(new Date()),
                path,
                errorMessages
            )
        );
    }
}

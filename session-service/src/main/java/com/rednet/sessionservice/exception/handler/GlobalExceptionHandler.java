package com.rednet.sessionservice.exception.handler;

import com.rednet.sessionservice.exception.impl.InvalidTokenException;
import com.rednet.sessionservice.exception.impl.SessionNotFoundException;
import com.rednet.sessionservice.exception.impl.SessionRemovingException;
import com.rednet.sessionservice.exception.impl.UserSessionsNotFound;
import com.rednet.sessionservice.exception.impl.UserSessionsRemovingException;
import com.rednet.sessionservice.exception.ErrorResponseMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.annotation.ParametersAreNonnullByDefault;
import java.time.Instant;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @Override
    @ParametersAreNonnullByDefault
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
        HttpRequestMethodNotSupportedException ex,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        return generateErrorResponse(BAD_REQUEST, extractPath(request), ex.getMessage());
    }

    @Override
    @ParametersAreNonnullByDefault
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
        HttpMediaTypeNotSupportedException ex,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        return generateErrorResponse(BAD_REQUEST, extractPath(request), ex.getMessage());
    }

    @Override
    @ParametersAreNonnullByDefault
    protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(
        HttpMediaTypeNotAcceptableException ex,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        return generateErrorResponse(BAD_REQUEST, extractPath(request), ex.getMessage());
    }

    @Override
    @ParametersAreNonnullByDefault
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
        MissingServletRequestParameterException ex,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        return generateErrorResponse(BAD_REQUEST, extractPath(request), ex.getMessage());
    }

    @Override
    @ParametersAreNonnullByDefault
    protected ResponseEntity<Object> handleMissingServletRequestPart(
        MissingServletRequestPartException ex,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        return generateErrorResponse(BAD_REQUEST, extractPath(request), ex.getMessage());
    }

    @Override
    @ParametersAreNonnullByDefault
    protected ResponseEntity<Object> handleServletRequestBindingException(
        ServletRequestBindingException ex,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        return generateErrorResponse(BAD_REQUEST, extractPath(request), ex.getMessage());
    }

    @Override
    @ParametersAreNonnullByDefault
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        return generateErrorResponse(BAD_REQUEST, extractPath(request), ex.getMessage());
    }

    @Override
    @ParametersAreNonnullByDefault
    protected ResponseEntity<Object> handleNoHandlerFoundException(
        NoHandlerFoundException ex,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        return generateErrorResponse(NOT_FOUND, extractPath(request), ex.getMessage());
    }

    @Override
    @ParametersAreNonnullByDefault
    protected ResponseEntity<Object> handleAsyncRequestTimeoutException(
        AsyncRequestTimeoutException ex,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        return generateErrorResponse(SERVICE_UNAVAILABLE, extractPath(request), ex.getMessage());
    }

    @Override
    @ParametersAreNonnullByDefault
    protected ResponseEntity<Object> handleHttpMessageNotWritable(
        HttpMessageNotWritableException ex,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        return generateErrorResponse(INTERNAL_SERVER_ERROR, extractPath(request), ex.getMessage());
    }

    @Override
    @ParametersAreNonnullByDefault
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
        HttpMessageNotReadableException ex,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        return generateErrorResponse(BAD_REQUEST, extractPath(request), ex.getMessage());
    }

    @ExceptionHandler(InvalidTokenException.class)
    protected ResponseEntity<Object> handleInvalidToken(
        InvalidTokenException ex,
        HttpServletRequest request
    ) {
        return generateErrorResponse(BAD_REQUEST ,request.getServletPath(), ex.getMessage());
    }

    @ExceptionHandler(value = {SessionNotFoundException.class, UserSessionsNotFound.class})
    protected ResponseEntity<Object> handleSessionsNotFound(
        RuntimeException ex,
        HttpServletRequest request
    ) {
        return generateErrorResponse(NOT_FOUND, request.getServletPath(), ex.getMessage());
    }

    @ExceptionHandler(value = {SessionRemovingException.class, UserSessionsRemovingException.class})
    protected ResponseEntity<Object> handleRemovingSession(
        RuntimeException ex,
        HttpServletRequest request
    ) {
        return generateErrorResponse(INTERNAL_SERVER_ERROR, request.getServletPath(), ex.getMessage());
    }

    private ResponseEntity<Object> generateErrorResponse(
        HttpStatus httpStatus,
        String path,
        String errorMessage
    ) {
        return ResponseEntity.status(httpStatus.value()).body(
            new ErrorResponseMessage(
                httpStatus.name(),
                Instant.now(),
                path,
                errorMessage
            )
        );
    }

    private String extractPath(WebRequest request) {
        try {
            return ((ServletWebRequest) request).getRequest().getServletPath();
        } catch (ClassCastException ex) {
            return "";
        }
    }
}

package com.securebank.config;

import com.securebank.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        String message = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        return error(HttpStatus.BAD_REQUEST, "Validation Failed", message, request);
    }

    @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class,
            LockedException.class, DisabledException.class})
    public ResponseEntity<ApiError> handleAuth(Exception ex, HttpServletRequest req) {
        return error(HttpStatus.UNAUTHORIZED, "Authentication Failed",
                ex.getMessage(), req);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ApiError> handleFraud(SecurityException ex,
                                                HttpServletRequest req) {
        return error(HttpStatus.FORBIDDEN, "Transaction Blocked",
                ex.getMessage(), req);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleNotFound(IllegalArgumentException ex,
                                                   HttpServletRequest req) {
        return error(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), req);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalState(IllegalStateException ex,
                                                       HttpServletRequest req) {
        return error(HttpStatus.CONFLICT, "Business Rule Violation",
                ex.getMessage(), req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex,
                                                  HttpServletRequest req) {
        log.error("Unhandled error", ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Error",
                "Unexpected error occurred", req);
    }

    private ResponseEntity<ApiError> error(HttpStatus status, String error,
                                           String message,
                                           HttpServletRequest req) {
        return ResponseEntity.status(status).body(
                new ApiError(status.value(), error, message,
                        req.getRequestURI(), LocalDateTime.now()));
    }
}

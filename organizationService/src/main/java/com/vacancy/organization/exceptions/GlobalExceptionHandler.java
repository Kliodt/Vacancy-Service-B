package com.vacancy.organization.exceptions;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // for @Valid annotation
    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValid(WebExchangeBindException e) {
        log.error("Validation error: {}", e.getMessage());
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(RequestException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValid(RequestException e) {
        return e.responseEntity();
    }

    // for @PreAuthorize hasRole
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAuthDenied(AuthorizationDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        log.error("Runtime error ({}): {}", e.getClass().toString(), e.getMessage(), e);
        return ResponseEntity.badRequest().body("Error :)");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception e) {
        log.error("Internal error ({}): {}", e.getClass().toString(), e.getMessage(), e);
        return ResponseEntity.status(500).body("Internal error :)");
    }
}
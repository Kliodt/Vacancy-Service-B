package com.vacancy.organization.exceptions;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // for @Valid annotation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
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

    // if this or other service is not responding
    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<String> handleMethodArgumentNotValid(ServiceException e) {
        log.error("Service unavailable: {}", e.getMessage(), e);
        return ResponseEntity.status(503).body("Service unavailable");
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        log.error("Runtime error: {}", e.getMessage());
        return ResponseEntity.badRequest().body("Error");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception e) {
        log.error("Internal error: {}", e.getMessage(), e);
        return ResponseEntity.status(500).body("Internal error");
    }
}
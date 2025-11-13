package com.vacancy.user.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;


public class RequestException extends RuntimeException {
    public final HttpStatus code;

    public RequestException(HttpStatus code, String message) {
        super(message);
        this.code = code;
    }

    public ResponseEntity<Map<String, String>> responseEntity() {
        return ResponseEntity.status(code.value()).body(Map.of(
                "Error", getMessage()
        ));
    }
}

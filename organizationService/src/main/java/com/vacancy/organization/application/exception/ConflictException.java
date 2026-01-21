package com.vacancy.organization.application.exception;

/**
 * Exception - Конфликт (например, дублирование уникального поля)
 * HTTP Status: 409 CONFLICT
 */
public class ConflictException extends DomainException {

    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}

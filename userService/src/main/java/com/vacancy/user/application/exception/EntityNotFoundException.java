package com.vacancy.user.application.exception;

/**
 * Exception - Сущность не найдена
 * HTTP Status: 404 NOT_FOUND
 */
public class EntityNotFoundException extends DomainException {

    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

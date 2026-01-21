package com.vacancy.user.application.exception;

/**
 * Exception - Доступ запрещен
 * HTTP Status: 403 FORBIDDEN
 */
public class AccessDeniedException extends DomainException {

    public AccessDeniedException(String message) {
        super(message);
    }
}

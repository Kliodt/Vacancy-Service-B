package com.vacancy.vacancy.application.exception;

/**
 * Domain Exception - доступ запрещен
 * Маппится на HTTP 403
 */
public class AccessDeniedException extends DomainException {
    public AccessDeniedException(String message) {
        super(message);
    }
}

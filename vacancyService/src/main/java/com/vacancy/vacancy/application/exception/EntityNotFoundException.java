package com.vacancy.vacancy.application.exception;

/**
 * Domain Exception - сущность не найдена
 * Маппится на HTTP 404
 */
public class EntityNotFoundException extends DomainException {
    public EntityNotFoundException(String message) {
        super(message);
    }
}

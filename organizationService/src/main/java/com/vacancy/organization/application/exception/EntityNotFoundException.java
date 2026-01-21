package com.vacancy.organization.application.exception;

/**
 * Исключение когда сущность не найдена (404)
 */
public class EntityNotFoundException extends DomainException {
    public EntityNotFoundException(String message) {
        super(message);
    }
}

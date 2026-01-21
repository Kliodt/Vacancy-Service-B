package com.vacancy.organization.application.exception;

/**
 * Исключение для доступа запрещено - организация не владеет ресурсом (403)
 */
public class AccessDeniedException extends DomainException {
    public AccessDeniedException(String message) {
        super(message);
    }
}

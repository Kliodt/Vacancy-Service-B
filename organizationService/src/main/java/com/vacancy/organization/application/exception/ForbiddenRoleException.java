package com.vacancy.organization.application.exception;

/**
 * Исключение для недостаточных прав роли (403)
 */
public class ForbiddenRoleException extends DomainException {
    public ForbiddenRoleException(String message) {
        super(message);
    }
}

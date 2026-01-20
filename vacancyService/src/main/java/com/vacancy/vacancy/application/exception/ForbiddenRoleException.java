package com.vacancy.vacancy.application.exception;

/**
 * Domain Exception - роль не разрешена
 * Маппится на HTTP 403
 */
public class ForbiddenRoleException extends DomainException {
    public ForbiddenRoleException(String message) {
        super(message);
    }
}

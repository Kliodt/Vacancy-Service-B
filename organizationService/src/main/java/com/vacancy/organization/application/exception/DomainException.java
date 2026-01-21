package com.vacancy.organization.application.exception;

/**
 * Базовое исключение для ошибок бизнес-логики
 */
public abstract class DomainException extends RuntimeException {
    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}

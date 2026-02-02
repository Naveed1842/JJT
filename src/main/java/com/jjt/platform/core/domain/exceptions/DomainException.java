package com.jjt.platform.core.domain.exceptions;

/**
 * Base type for all domain rule violations in the core model.
 */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.jjt.platform.core.domain.exceptions;

/**
 * Thrown when sponsorship invariants are violated.
 */
public class SponsorshipInvariantViolationException extends DomainException {

    public SponsorshipInvariantViolationException(String message) {
        super(message);
    }
}

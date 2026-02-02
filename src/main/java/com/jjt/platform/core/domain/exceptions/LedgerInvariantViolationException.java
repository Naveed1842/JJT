package com.jjt.platform.core.domain.exceptions;

/**
 * Thrown when a ledger invariant is violated (e.g., duplicate month or invalid correction).
 */
public class LedgerInvariantViolationException extends DomainException {

    public LedgerInvariantViolationException(String message) {
        super(message);
    }
}

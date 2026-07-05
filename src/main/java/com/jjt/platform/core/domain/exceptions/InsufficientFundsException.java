package com.jjt.platform.core.domain.exceptions;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException {

    private final BigDecimal currentBalance;
    private final BigDecimal debitAmount;
    private final BigDecimal minReserve;
    private final String currency;

    public InsufficientFundsException(BigDecimal currentBalance, BigDecimal debitAmount,
                                      BigDecimal minReserve, String currency) {
        super("Fund balance would fall below minimum reserve after this debit");
        this.currentBalance = currentBalance;
        this.debitAmount = debitAmount;
        this.minReserve = minReserve;
        this.currency = currency;
    }

    public BigDecimal getCurrentBalance() { return currentBalance; }
    public BigDecimal getDebitAmount() { return debitAmount; }
    public BigDecimal getMinReserve() { return minReserve; }
    public String getCurrency() { return currency; }
}

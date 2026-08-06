package com.jjt.platform.core.domain.entity;

import java.math.BigDecimal;
import java.util.UUID;

public final class PayrollProfile {
    private final UUID id;
    private final UUID personId;
    private final SalaryType salaryType;
    private final BigDecimal amount;
    private final String currency;
    private final UUID costCentreId;
    private final PaymentMethod paymentMethod;
    private final boolean active;

    public PayrollProfile(UUID id, UUID personId, SalaryType salaryType, BigDecimal amount,
                          String currency, UUID costCentreId, PaymentMethod paymentMethod,
                          boolean active) {
        this.id = id; this.personId = personId; this.salaryType = salaryType;
        this.amount = amount; this.currency = currency;
        this.costCentreId = costCentreId; this.paymentMethod = paymentMethod;
        this.active = active;
    }

    public UUID getId() { return id; }
    public UUID getPersonId() { return personId; }
    public SalaryType getSalaryType() { return salaryType; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public UUID getCostCentreId() { return costCentreId; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public boolean isActive() { return active; }
}

package com.jjt.platform.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "payroll_profiles")
public class PayrollProfileEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "person_id", nullable = false, unique = true)
    private UUID personId;

    @Column(name = "salary_type", nullable = false, length = 20)
    private String salaryType;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "cost_centre_id")
    private UUID costCentreId;

    @Column(name = "payment_method", nullable = false, length = 20)
    private String paymentMethod;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    protected PayrollProfileEntity() {}

    public PayrollProfileEntity(UUID id, UUID personId, String salaryType, BigDecimal amount,
                                String currency, UUID costCentreId, String paymentMethod) {
        this.id = id; this.personId = personId; this.salaryType = salaryType;
        this.amount = amount; this.currency = currency;
        this.costCentreId = costCentreId; this.paymentMethod = paymentMethod;
        this.active = true;
    }

    public UUID getId() { return id; }
    public UUID getPersonId() { return personId; }
    public String getSalaryType() { return salaryType; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public UUID getCostCentreId() { return costCentreId; }
    public void setCostCentreId(UUID costCentreId) { this.costCentreId = costCentreId; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}

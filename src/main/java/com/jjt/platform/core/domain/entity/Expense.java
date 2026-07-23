package com.jjt.platform.core.domain.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public final class Expense {
    private final UUID id;
    private final UUID orgId;
    private final UUID vendorId;
    private final String payeeText;
    private final Integer categoryId;
    private final UUID costCentreId;
    private final UUID missionNodeId;
    private final UUID periodId;
    private final String invoiceRef;
    private final BigDecimal amount;
    private final String currency;
    private final String description;
    private final ExpenseStatus status;
    private final LocalDate paidAt;
    private final UUID txId;
    private final UUID createdBy;
    private final Instant createdAt;

    public Expense(UUID id, UUID orgId, UUID vendorId, String payeeText,
                   Integer categoryId, UUID costCentreId, UUID missionNodeId,
                   UUID periodId, String invoiceRef, BigDecimal amount, String currency,
                   String description, ExpenseStatus status, LocalDate paidAt, UUID txId,
                   UUID createdBy, Instant createdAt) {
        this.id = id; this.orgId = orgId; this.vendorId = vendorId;
        this.payeeText = payeeText; this.categoryId = categoryId;
        this.costCentreId = costCentreId; this.missionNodeId = missionNodeId;
        this.periodId = periodId; this.invoiceRef = invoiceRef;
        this.amount = amount; this.currency = currency;
        this.description = description; this.status = status;
        this.paidAt = paidAt; this.txId = txId;
        this.createdBy = createdBy; this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getOrgId() { return orgId; }
    public UUID getVendorId() { return vendorId; }
    public String getPayeeText() { return payeeText; }
    public Integer getCategoryId() { return categoryId; }
    public UUID getCostCentreId() { return costCentreId; }
    public UUID getMissionNodeId() { return missionNodeId; }
    public UUID getPeriodId() { return periodId; }
    public String getInvoiceRef() { return invoiceRef; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getDescription() { return description; }
    public ExpenseStatus getStatus() { return status; }
    public LocalDate getPaidAt() { return paidAt; }
    public UUID getTxId() { return txId; }
    public UUID getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }

    public Expense withStatus(ExpenseStatus newStatus) {
        return new Expense(id, orgId, vendorId, payeeText, categoryId, costCentreId,
                missionNodeId, periodId, invoiceRef, amount, currency, description,
                newStatus, paidAt, txId, createdBy, createdAt);
    }

    public Expense withPaid(LocalDate date, UUID transactionId) {
        return new Expense(id, orgId, vendorId, payeeText, categoryId, costCentreId,
                missionNodeId, periodId, invoiceRef, amount, currency, description,
                ExpenseStatus.PAID, date, transactionId, createdBy, createdAt);
    }
}

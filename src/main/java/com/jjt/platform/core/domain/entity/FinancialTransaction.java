package com.jjt.platform.core.domain.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public final class FinancialTransaction {
    private final UUID id;
    private final UUID orgId;
    private final UUID fundAccountId;
    private final Integer categoryId;
    private final UUID costCentreId;
    private final UUID missionNodeId;
    private final String sourceType;
    private final UUID sourceId;
    private final UUID transferGroup;
    private final FinancialTransactionType type;
    private final BigDecimal amount;
    private final String currency;
    private final LocalDate effectiveDate;
    private final String description;
    private final UUID createdBy;
    private final Instant createdAt;

    public FinancialTransaction(UUID id, UUID orgId, UUID fundAccountId, Integer categoryId,
                                UUID costCentreId, UUID missionNodeId, String sourceType,
                                UUID sourceId, UUID transferGroup, FinancialTransactionType type,
                                BigDecimal amount, String currency, LocalDate effectiveDate,
                                String description, UUID createdBy, Instant createdAt) {
        this.id = id; this.orgId = orgId; this.fundAccountId = fundAccountId;
        this.categoryId = categoryId; this.costCentreId = costCentreId;
        this.missionNodeId = missionNodeId; this.sourceType = sourceType;
        this.sourceId = sourceId; this.transferGroup = transferGroup;
        this.type = type; this.amount = amount; this.currency = currency;
        this.effectiveDate = effectiveDate; this.description = description;
        this.createdBy = createdBy; this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getOrgId() { return orgId; }
    public UUID getFundAccountId() { return fundAccountId; }
    public Integer getCategoryId() { return categoryId; }
    public UUID getCostCentreId() { return costCentreId; }
    public UUID getMissionNodeId() { return missionNodeId; }
    public String getSourceType() { return sourceType; }
    public UUID getSourceId() { return sourceId; }
    public UUID getTransferGroup() { return transferGroup; }
    public FinancialTransactionType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public LocalDate getEffectiveDate() { return effectiveDate; }
    public String getDescription() { return description; }
    public UUID getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }
}

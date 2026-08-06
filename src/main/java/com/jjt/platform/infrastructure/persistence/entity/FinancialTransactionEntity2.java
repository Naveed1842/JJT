package com.jjt.platform.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * JPA entity for the financial_transactions journal (Layer 1 — append-only).
 * Named with suffix "2" to avoid collision with the existing FundTransactionEntity.
 */
@Entity
@Table(name = "financial_transactions")
public class FinancialTransactionEntity2 {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "org_id", nullable = false, updatable = false)
    private UUID orgId;

    @Column(name = "fund_account_id", updatable = false)
    private UUID fundAccountId;

    @Column(name = "category_id", updatable = false)
    private Integer categoryId;

    @Column(name = "cost_centre_id", updatable = false)
    private UUID costCentreId;

    @Column(name = "mission_node_id", updatable = false)
    private UUID missionNodeId;

    @Column(name = "source_type", nullable = false, length = 30, updatable = false)
    private String sourceType;

    @Column(name = "source_id", updatable = false)
    private UUID sourceId;

    @Column(name = "transfer_group", updatable = false)
    private UUID transferGroup;

    @Column(name = "type", nullable = false, length = 20, updatable = false)
    private String type;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2, updatable = false)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3, updatable = false)
    private String currency;

    @Column(name = "effective_date", nullable = false, updatable = false)
    private LocalDate effectiveDate;

    @Column(name = "description", columnDefinition = "TEXT", updatable = false)
    private String description;

    @Column(name = "created_by", updatable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected FinancialTransactionEntity2() {}

    public FinancialTransactionEntity2(UUID id, UUID orgId, UUID fundAccountId, Integer categoryId,
                                       UUID costCentreId, UUID missionNodeId, String sourceType,
                                       UUID sourceId, UUID transferGroup, String type,
                                       BigDecimal amount, String currency, LocalDate effectiveDate,
                                       String description, UUID createdBy) {
        this.id = id; this.orgId = orgId; this.fundAccountId = fundAccountId;
        this.categoryId = categoryId; this.costCentreId = costCentreId;
        this.missionNodeId = missionNodeId; this.sourceType = sourceType;
        this.sourceId = sourceId; this.transferGroup = transferGroup;
        this.type = type; this.amount = amount; this.currency = currency;
        this.effectiveDate = effectiveDate; this.description = description;
        this.createdBy = createdBy; this.createdAt = Instant.now();
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
    public String getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public LocalDate getEffectiveDate() { return effectiveDate; }
    public String getDescription() { return description; }
    public UUID getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }
}

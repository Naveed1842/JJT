package com.jjt.platform.infrastructure.persistence.entity;

import com.jjt.platform.core.domain.entity.FundTransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "fund_transactions")
@Immutable
public class FundTransactionEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "fund_account_id", nullable = false, updatable = false)
    private UUID fundAccountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 10, updatable = false)
    private FundTransactionType transactionType;

    @Column(name = "amount", nullable = false, precision = 14, scale = 2, updatable = false)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3, updatable = false)
    private String currency;

    @Column(name = "reason", nullable = false, length = 100, updatable = false)
    private String reason;

    @Column(name = "description", updatable = false)
    private String description;

    @Column(name = "external_reference", length = 200, updatable = false)
    private String externalReference;

    @Column(name = "ledger_entry_id", updatable = false)
    private UUID ledgerEntryId;

    @Column(name = "created_by", updatable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected FundTransactionEntity() {}

    public FundTransactionEntity(UUID id, UUID fundAccountId, FundTransactionType transactionType,
                                 BigDecimal amount, String currency, String reason,
                                 String description, String externalReference,
                                 UUID ledgerEntryId, UUID createdBy, Instant createdAt) {
        this.id = id;
        this.fundAccountId = fundAccountId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.currency = currency;
        this.reason = reason;
        this.description = description;
        this.externalReference = externalReference;
        this.ledgerEntryId = ledgerEntryId;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getFundAccountId() { return fundAccountId; }
    public FundTransactionType getTransactionType() { return transactionType; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getReason() { return reason; }
    public String getDescription() { return description; }
    public String getExternalReference() { return externalReference; }
    public UUID getLedgerEntryId() { return ledgerEntryId; }
    public UUID getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }
}

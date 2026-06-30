package com.jjt.platform.core.domain.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class FundTransaction {

    private final UUID id;
    private final UUID fundAccountId;
    private final FundTransactionType transactionType;
    private final BigDecimal amount;
    private final String currency;
    private final String reason;
    private final String description;
    private final String externalReference;
    private final UUID ledgerEntryId;
    private final UUID createdBy;
    private final Instant createdAt;

    public FundTransaction(UUID id, UUID fundAccountId, FundTransactionType transactionType,
                           BigDecimal amount, String currency, String reason,
                           String description, String externalReference,
                           UUID ledgerEntryId, UUID createdBy, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.fundAccountId = Objects.requireNonNull(fundAccountId, "fundAccountId must not be null");
        this.transactionType = Objects.requireNonNull(transactionType, "transactionType must not be null");
        this.amount = Objects.requireNonNull(amount, "amount must not be null");
        this.currency = Objects.requireNonNull(currency, "currency must not be null");
        this.reason = Objects.requireNonNull(reason, "reason must not be null");
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return id.equals(((FundTransaction) o).id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }
}

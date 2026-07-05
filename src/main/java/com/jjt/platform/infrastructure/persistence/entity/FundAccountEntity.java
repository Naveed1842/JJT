package com.jjt.platform.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "fund_accounts")
public class FundAccountEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "min_reserve", nullable = false, precision = 14, scale = 2)
    private BigDecimal minReserve;

    @Column(name = "organisation_id")
    private UUID organisationId;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected FundAccountEntity() {}

    public FundAccountEntity(UUID id, String name, String currency, BigDecimal minReserve,
                              UUID organisationId, UUID createdBy, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.currency = currency;
        this.minReserve = minReserve;
        this.organisationId = organisationId;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getCurrency() { return currency; }
    public BigDecimal getMinReserve() { return minReserve; }
    public UUID getOrganisationId() { return organisationId; }
    public UUID getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }
}

package com.jjt.platform.core.domain.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class FundAccount {

    private final UUID id;
    private final String name;
    private final String currency;
    private final BigDecimal minReserve;
    private final UUID organisationId;
    private final Instant createdAt;

    public FundAccount(UUID id, String name, String currency, BigDecimal minReserve,
                       UUID organisationId, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.currency = Objects.requireNonNull(currency, "currency must not be null");
        this.minReserve = minReserve != null ? minReserve : BigDecimal.ZERO;
        this.organisationId = organisationId;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getCurrency() { return currency; }
    public BigDecimal getMinReserve() { return minReserve; }
    public UUID getOrganisationId() { return organisationId; }
    public Instant getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return id.equals(((FundAccount) o).id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }
}

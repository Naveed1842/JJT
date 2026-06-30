package com.jjt.platform.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "organisations")
public class OrganisationEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "slug", nullable = false, unique = true, length = 64)
    private String slug;

    @Column(name = "base_currency", nullable = false, length = 3)
    private String baseCurrency;

    @Column(name = "payment_due_day", nullable = false)
    private int paymentDueDay;

    @Column(name = "min_fund_reserve", nullable = false, precision = 14, scale = 2)
    private BigDecimal minFundReserve;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected OrganisationEntity() {}

    public OrganisationEntity(UUID id, String name, String slug, String baseCurrency,
                              int paymentDueDay, BigDecimal minFundReserve,
                              boolean active, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.baseCurrency = baseCurrency;
        this.paymentDueDay = paymentDueDay;
        this.minFundReserve = minFundReserve;
        this.active = active;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public String getBaseCurrency() { return baseCurrency; }
    public int getPaymentDueDay() { return paymentDueDay; }
    public BigDecimal getMinFundReserve() { return minFundReserve; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
}

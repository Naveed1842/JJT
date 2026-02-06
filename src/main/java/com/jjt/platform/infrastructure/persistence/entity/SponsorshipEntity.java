package com.jjt.platform.infrastructure.persistence.entity;

import com.jjt.platform.core.domain.entity.SponsorshipStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sponsorships", uniqueConstraints = @UniqueConstraint(name = "uk_sponsor_child_start", columnNames = {"sponsor_id", "child_id", "start_month"}))
public class SponsorshipEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sponsor_id", nullable = false)
    private SponsorEntity sponsor;

    @Column(name = "child_id", nullable = false)
    private UUID childId;

    @Column(name = "start_month", nullable = false, length = 7)
    private String startMonth; // YYYY-MM

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private SponsorshipStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    protected SponsorshipEntity() {
    }

    public SponsorshipEntity(UUID id, SponsorEntity sponsor, UUID childId, String startMonth,
                             SponsorshipStatus status, Instant createdAt, Instant expiresAt) {
        this.id = id;
        this.sponsor = sponsor;
        this.childId = childId;
        this.startMonth = startMonth;
        this.status = status;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public UUID getId() {
        return id;
    }

    public SponsorEntity getSponsor() {
        return sponsor;
    }

    public UUID getChildId() {
        return childId;
    }

    public String getStartMonth() {
        return startMonth;
    }

    public SponsorshipStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setStatus(SponsorshipStatus status) {
        this.status = status;
    }
}

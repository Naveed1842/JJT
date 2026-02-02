package com.jjt.platform.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

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

    protected SponsorshipEntity() {
    }

    public SponsorshipEntity(UUID id, SponsorEntity sponsor, UUID childId, String startMonth) {
        this.id = id;
        this.sponsor = sponsor;
        this.childId = childId;
        this.startMonth = startMonth;
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
}

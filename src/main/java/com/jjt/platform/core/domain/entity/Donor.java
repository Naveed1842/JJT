package com.jjt.platform.core.domain.entity;

import java.time.Instant;
import java.util.UUID;

public final class Donor {

    private final UUID id;
    private final UUID organisationId;
    private final String displayName;
    private final String email;
    private final String phone;
    private final DonorType donorType;
    private final String notes;
    private final UUID createdBy;
    private final Instant createdAt;

    public Donor(UUID id, UUID organisationId, String displayName, String email, String phone,
                 DonorType donorType, String notes, UUID createdBy, Instant createdAt) {
        this.id = id;
        this.organisationId = organisationId;
        this.displayName = displayName;
        this.email = email;
        this.phone = phone;
        this.donorType = donorType;
        this.notes = notes;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public static Donor createNew(UUID organisationId, String displayName, String email,
                                  String phone, DonorType donorType, String notes, UUID createdBy) {
        return new Donor(UUID.randomUUID(), organisationId, displayName, email, phone,
                donorType, notes, createdBy, Instant.now());
    }

    public UUID getId() { return id; }
    public UUID getOrganisationId() { return organisationId; }
    public String getDisplayName() { return displayName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public DonorType getDonorType() { return donorType; }
    public String getNotes() { return notes; }
    public UUID getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }
}

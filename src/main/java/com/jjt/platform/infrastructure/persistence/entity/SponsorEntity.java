package com.jjt.platform.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "sponsors")
public class SponsorEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "contact_email", nullable = false)
    private String contactEmail;

    @Column(name = "phone")
    private String phone;

    @Column(name = "organisation_id", nullable = false, updatable = false)
    private UUID organisationId;

    protected SponsorEntity() {
    }

    public SponsorEntity(UUID id, String displayName, String contactEmail, String phone, UUID organisationId) {
        this.id = id;
        this.displayName = displayName;
        this.contactEmail = contactEmail;
        this.phone = phone;
        this.organisationId = organisationId;
    }

    public UUID getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public String getPhone() {
        return phone;
    }

    public UUID getOrganisationId() { return organisationId; }

    public void updateProfile(String displayName, String phone) {
        this.displayName = displayName;
        this.phone = phone;
    }
}

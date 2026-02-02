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

    protected SponsorEntity() {
    }

    public SponsorEntity(UUID id, String displayName, String contactEmail) {
        this.id = id;
        this.displayName = displayName;
        this.contactEmail = contactEmail;
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
}

package com.jjt.platform.core.domain.entity;

import java.util.Objects;
import java.util.UUID;

/**
 * Sponsor is a commitment entity; no payments handled in Phase-1.
 */
public final class Sponsor {

    private final UUID id;
    private final String displayName;
    private final String contactEmail;

    public Sponsor(UUID id, String displayName, String contactEmail) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.displayName = Objects.requireNonNull(displayName, "displayName must not be null");
        if (displayName.isBlank()) {
            throw new IllegalArgumentException("displayName must not be blank");
        }
        this.contactEmail = Objects.requireNonNull(contactEmail, "contactEmail must not be null");
        if (contactEmail.isBlank()) {
            throw new IllegalArgumentException("contactEmail must not be blank");
        }
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sponsor sponsor = (Sponsor) o;
        return id.equals(sponsor.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}

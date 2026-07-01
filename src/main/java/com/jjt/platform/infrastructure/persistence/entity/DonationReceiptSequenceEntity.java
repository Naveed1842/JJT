package com.jjt.platform.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "donation_receipt_sequences")
public class DonationReceiptSequenceEntity {

    @EmbeddedId
    private SequenceId id;

    @Column(name = "last_sequence", nullable = false)
    private int lastSequence;

    protected DonationReceiptSequenceEntity() {}

    public DonationReceiptSequenceEntity(SequenceId id, int lastSequence) {
        this.id = id;
        this.lastSequence = lastSequence;
    }

    public SequenceId getId() { return id; }
    public int getLastSequence() { return lastSequence; }
    public void setLastSequence(int lastSequence) { this.lastSequence = lastSequence; }

    @Embeddable
    public static class SequenceId implements Serializable {

        @Column(name = "organisation_id", nullable = false)
        private UUID organisationId;

        @Column(name = "year", nullable = false)
        private int year;

        protected SequenceId() {}

        public SequenceId(UUID organisationId, int year) {
            this.organisationId = organisationId;
            this.year = year;
        }

        public UUID getOrganisationId() { return organisationId; }
        public int getYear() { return year; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SequenceId that)) return false;
            return year == that.year && Objects.equals(organisationId, that.organisationId);
        }

        @Override
        public int hashCode() { return Objects.hash(organisationId, year); }
    }
}

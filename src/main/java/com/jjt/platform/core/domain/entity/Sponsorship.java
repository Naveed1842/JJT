package com.jjt.platform.core.domain.entity;

import com.jjt.platform.core.domain.exceptions.DomainException;
import com.jjt.platform.core.domain.value.YearMonthValue;

import java.time.Instant;
import java.time.YearMonth;
import java.util.Objects;
import java.util.UUID;

/**
 * Sponsorship: a commitment from a sponsor to fund a child's education from a given month.
 *
 * Factory methods:
 *   createPending() — for new sponsorships entered via the admin panel (validates future start).
 *   restore()       — for reconstructing sponsorships from the database (no start-month validation).
 */
public final class Sponsorship {

    private final UUID id;
    private final UUID sponsorId;
    private final UUID childId;
    private final YearMonthValue startMonth;
    private final SponsorshipStatus status;
    private final Instant createdAt;
    private final Instant expiresAt;
    private final CommitmentType commitmentType;
    private final UUID createdBy;  // nullable for Phase 1 rows

    private Sponsorship(UUID id, UUID sponsorId, UUID childId, YearMonthValue startMonth,
                        SponsorshipStatus status, Instant createdAt, Instant expiresAt,
                        CommitmentType commitmentType, UUID createdBy) {
        this.id = Objects.requireNonNull(id);
        this.sponsorId = Objects.requireNonNull(sponsorId);
        this.childId = Objects.requireNonNull(childId);
        this.startMonth = Objects.requireNonNull(startMonth);
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.expiresAt = expiresAt;
        this.commitmentType = Objects.requireNonNull(commitmentType);
        this.createdBy = createdBy;
    }

    /** Create a brand-new PENDING sponsorship. Start month must be strictly in the future. */
    public static Sponsorship createPending(UUID id, UUID sponsorId, UUID childId,
                                            YearMonthValue startMonth, Instant now,
                                            CommitmentType commitmentType, UUID createdBy) {
        validateFutureStart(startMonth);
        return new Sponsorship(id, sponsorId, childId, startMonth,
                SponsorshipStatus.PENDING, now, null, commitmentType, createdBy);
    }

    /** Restore a sponsorship from the database. No start-month validation — historical data may have past months. */
    public static Sponsorship restore(UUID id, UUID sponsorId, UUID childId,
                                      YearMonthValue startMonth, SponsorshipStatus status,
                                      Instant createdAt, Instant expiresAt,
                                      CommitmentType commitmentType, UUID createdBy) {
        return new Sponsorship(id, sponsorId, childId, startMonth, status,
                createdAt, expiresAt, commitmentType, createdBy);
    }

    private static void validateFutureStart(YearMonthValue startMonth) {
        if (!startMonth.getValue().isAfter(YearMonth.now())) {
            throw new DomainException("Sponsorship startMonth must be in the future");
        }
    }

    public Sponsorship withStatus(SponsorshipStatus newStatus) {
        return new Sponsorship(id, sponsorId, childId, startMonth, newStatus,
                createdAt, expiresAt, commitmentType, createdBy);
    }

    public UUID getId() { return id; }
    public UUID getSponsorId() { return sponsorId; }
    public UUID getChildId() { return childId; }
    public YearMonthValue getStartMonth() { return startMonth; }
    public SponsorshipStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public CommitmentType getCommitmentType() { return commitmentType; }
    public UUID getCreatedBy() { return createdBy; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return id.equals(((Sponsorship) o).id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }
}

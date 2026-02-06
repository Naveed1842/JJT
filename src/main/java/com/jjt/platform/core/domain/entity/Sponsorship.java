package com.jjt.platform.core.domain.entity;

import com.jjt.platform.core.domain.exceptions.DomainException;
import com.jjt.platform.core.domain.value.YearMonthValue;

import java.time.Instant;
import java.time.YearMonth;
import java.util.Objects;
import java.util.UUID;

/**
 * Sponsorship is a future commitment; cannot backdate.
 */
public final class Sponsorship {

    private final UUID id;
    private final UUID sponsorId;
    private final UUID childId;
    private final YearMonthValue startMonth;
    private final SponsorshipStatus status;
    private final Instant createdAt;
    private final Instant expiresAt;

    private Sponsorship(UUID id, UUID sponsorId, UUID childId, YearMonthValue startMonth,
                        SponsorshipStatus status, Instant createdAt, Instant expiresAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.sponsorId = Objects.requireNonNull(sponsorId, "sponsorId must not be null");
        this.childId = Objects.requireNonNull(childId, "childId must not be null");
        this.startMonth = Objects.requireNonNull(startMonth, "startMonth must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.expiresAt = expiresAt;
    }

    public static Sponsorship createPending(UUID id, UUID sponsorId, UUID childId, YearMonthValue startMonth, Instant now, Instant expiresAt) {
        validateFutureStart(startMonth);
        return new Sponsorship(id, sponsorId, childId, startMonth, SponsorshipStatus.PENDING, now, expiresAt);
    }

    public static Sponsorship create(UUID id, UUID sponsorId, UUID childId, YearMonthValue startMonth,
                                     SponsorshipStatus status, Instant createdAt, Instant expiresAt) {
        validateFutureStart(startMonth);
        return new Sponsorship(id, sponsorId, childId, startMonth, status, createdAt, expiresAt);
    }

    private static void validateFutureStart(YearMonthValue startMonth) {
        YearMonth now = YearMonth.now();
        if (!startMonth.getValue().isAfter(now)) {
            throw new DomainException("Sponsorship startMonth must be in the future");
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getSponsorId() {
        return sponsorId;
    }

    public UUID getChildId() {
        return childId;
    }

    public YearMonthValue getStartMonth() {
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

    public Sponsorship withStatus(SponsorshipStatus newStatus) {
        return new Sponsorship(this.id, this.sponsorId, this.childId, this.startMonth, newStatus, this.createdAt, this.expiresAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sponsorship that = (Sponsorship) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}

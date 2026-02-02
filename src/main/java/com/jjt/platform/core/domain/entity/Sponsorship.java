package com.jjt.platform.core.domain.entity;

import com.jjt.platform.core.domain.exceptions.DomainException;
import com.jjt.platform.core.domain.value.YearMonthValue;

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

    private Sponsorship(UUID id, UUID sponsorId, UUID childId, YearMonthValue startMonth) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.sponsorId = Objects.requireNonNull(sponsorId, "sponsorId must not be null");
        this.childId = Objects.requireNonNull(childId, "childId must not be null");
        this.startMonth = Objects.requireNonNull(startMonth, "startMonth must not be null");
    }

    public static Sponsorship create(UUID id, UUID sponsorId, UUID childId, YearMonthValue startMonth) {
        validateFutureStart(startMonth);
        return new Sponsorship(id, sponsorId, childId, startMonth);
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

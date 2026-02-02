package com.jjt.platform.core.domain.entity;

import com.jjt.platform.core.domain.exceptions.DomainException;
import com.jjt.platform.core.domain.value.YearMonthValue;

import java.util.Objects;
import java.util.UUID;

/**
 * Immutable monthly progress update tied to a ledger month.
 */
public final class ProgressUpdate {

    private final UUID id;
    private final UUID childId;
    private final YearMonthValue month;
    private final String summary;

    private ProgressUpdate(UUID id, UUID childId, YearMonthValue month, String summary) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.childId = Objects.requireNonNull(childId, "childId must not be null");
        this.month = Objects.requireNonNull(month, "month must not be null");
        this.summary = Objects.requireNonNull(summary, "summary must not be null");
        if (summary.isBlank()) {
            throw new IllegalArgumentException("summary must not be blank");
        }
    }

    public static ProgressUpdate create(UUID id, UUID childId, YearMonthValue month, String summary, EducationSupportLedger ledger) {
        Objects.requireNonNull(ledger, "ledger must not be null");
        if (!ledger.getChildId().equals(childId)) {
            throw new DomainException("ProgressUpdate childId must match ledger childId");
        }
        if (!ledger.hasEntryFor(month)) {
            throw new DomainException("ProgressUpdate month must match an existing ledger entry");
        }
        return new ProgressUpdate(id, childId, month, summary);
    }

    public UUID getId() {
        return id;
    }

    public UUID getChildId() {
        return childId;
    }

    public YearMonthValue getMonth() {
        return month;
    }

    public String getSummary() {
        return summary;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProgressUpdate that = (ProgressUpdate) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}

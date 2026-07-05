package com.jjt.platform.core.domain.entity;

import com.jjt.platform.core.domain.exceptions.DomainException;
import com.jjt.platform.core.domain.value.YearMonthValue;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class ProgressUpdate {
    private final UUID id;
    private final UUID childId;
    private final YearMonthValue month;
    private final String summary;
    private final UUID createdBy;
    private final Instant createdAt;

    private ProgressUpdate(UUID id, UUID childId, YearMonthValue month, String summary, UUID createdBy, Instant createdAt) {
        this.id = Objects.requireNonNull(id);
        this.childId = Objects.requireNonNull(childId);
        this.month = Objects.requireNonNull(month);
        this.summary = Objects.requireNonNull(summary);
        if (summary.isBlank()) throw new IllegalArgumentException("summary must not be blank");
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public static ProgressUpdate create(UUID id, UUID childId, YearMonthValue month, String summary, EducationSupportLedger ledger, UUID createdBy, Instant createdAt) {
        Objects.requireNonNull(ledger, "ledger must not be null");
        if (!ledger.getChildId().equals(childId)) throw new DomainException("ProgressUpdate childId must match ledger childId");
        if (!ledger.hasEntryFor(month)) throw new DomainException("ProgressUpdate month must match an existing ledger entry");
        return new ProgressUpdate(id, childId, month, summary, createdBy, createdAt);
    }

    public UUID getId() { return id; }
    public UUID getChildId() { return childId; }
    public YearMonthValue getMonth() { return month; }
    public String getSummary() { return summary; }
    public UUID getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }

    @Override public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; return id.equals(((ProgressUpdate) o).id); }
    @Override public int hashCode() { return id.hashCode(); }
}

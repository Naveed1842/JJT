package com.jjt.platform.core.domain.entity;

import com.jjt.platform.core.domain.value.Money;
import com.jjt.platform.core.domain.value.YearMonthValue;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable ledger entry: one per child per month.
 * createdBy and createdAt are nullable for Phase 1 historical rows; all Phase 2+ rows must supply them.
 */
public final class LedgerEntry {

    private final UUID id;
    private final UUID childId;
    private final YearMonthValue month;
    private final Money educationCost;
    private final CoverageType coverageType;
    private final UUID createdBy;   // nullable for Phase 1 rows
    private final Instant createdAt; // nullable for Phase 1 rows

    public LedgerEntry(UUID id, UUID childId, YearMonthValue month, Money educationCost,
                       CoverageType coverageType, UUID createdBy, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.childId = Objects.requireNonNull(childId, "childId must not be null");
        this.month = Objects.requireNonNull(month, "month must not be null");
        this.educationCost = Objects.requireNonNull(educationCost, "educationCost must not be null");
        this.coverageType = Objects.requireNonNull(coverageType, "coverageType must not be null");
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getChildId() { return childId; }
    public YearMonthValue getMonth() { return month; }
    public Money getEducationCost() { return educationCost; }
    public CoverageType getCoverageType() { return coverageType; }
    public UUID getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return id.equals(((LedgerEntry) o).id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }
}

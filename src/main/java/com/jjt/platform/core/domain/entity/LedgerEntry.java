package com.jjt.platform.core.domain.entity;

import com.jjt.platform.core.domain.value.Money;
import com.jjt.platform.core.domain.value.YearMonthValue;

import java.util.Objects;
import java.util.UUID;

/**
 * Immutable ledger entry: one per child per month. Corrections create new instances.
 */
public final class LedgerEntry {

    private final UUID id;
    private final UUID childId;
    private final YearMonthValue month;
    private final Money educationCost;

    public LedgerEntry(UUID id, UUID childId, YearMonthValue month, Money educationCost) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.childId = Objects.requireNonNull(childId, "childId must not be null");
        this.month = Objects.requireNonNull(month, "month must not be null");
        this.educationCost = Objects.requireNonNull(educationCost, "educationCost must not be null");
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

    public Money getEducationCost() {
        return educationCost;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LedgerEntry that = (LedgerEntry) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}

package com.jjt.platform.core.domain.entity;

import com.jjt.platform.core.domain.exceptions.LedgerInvariantViolationException;
import com.jjt.platform.core.domain.value.YearMonthValue;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Per-child ledger. Append-only: adding an entry returns a new ledger instance with the entry applied.
 */
public final class EducationSupportLedger {

    private final UUID id;
    private final UUID childId;
    private final Map<YearMonthValue, LedgerEntry> entriesByMonth;

    private EducationSupportLedger(UUID id, UUID childId, Map<YearMonthValue, LedgerEntry> entriesByMonth) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.childId = Objects.requireNonNull(childId, "childId must not be null");
        this.entriesByMonth = Collections.unmodifiableMap(entriesByMonth);
    }

    public static EducationSupportLedger create(UUID id, UUID childId) {
        return new EducationSupportLedger(id, childId, new LinkedHashMap<>());
    }

    public EducationSupportLedger appendEntry(LedgerEntry entry) {
        Objects.requireNonNull(entry, "entry must not be null");
        if (!entry.getChildId().equals(childId)) {
            throw new LedgerInvariantViolationException("Ledger entry childId does not match ledger childId");
        }
        if (entriesByMonth.containsKey(entry.getMonth())) {
            throw new LedgerInvariantViolationException("Ledger already has entry for month " + entry.getMonth());
        }
        Map<YearMonthValue, LedgerEntry> updated = new LinkedHashMap<>(entriesByMonth);
        updated.put(entry.getMonth(), entry);
        return new EducationSupportLedger(this.id, this.childId, updated);
    }

    public boolean hasEntryFor(YearMonthValue month) {
        return entriesByMonth.containsKey(month);
    }

    public Map<YearMonthValue, LedgerEntry> getEntriesByMonth() {
        return entriesByMonth;
    }

    public UUID getId() {
        return id;
    }

    public UUID getChildId() {
        return childId;
    }
}

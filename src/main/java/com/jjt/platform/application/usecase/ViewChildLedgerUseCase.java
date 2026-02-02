package com.jjt.platform.application.usecase;

import com.jjt.platform.core.domain.entity.EducationSupportLedger;
import com.jjt.platform.core.domain.entity.LedgerEntry;
import com.jjt.platform.core.domain.value.YearMonthValue;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Use case: Read-only view of a child's ledger entries.
 */
public class ViewChildLedgerUseCase {

    public LedgerView view(EducationSupportLedger ledger) {
        Objects.requireNonNull(ledger, "ledger must not be null");
        Map<YearMonthValue, LedgerEntry> entries = ledger.getEntriesByMonth();
        return new LedgerView(ledger.getChildId(), List.copyOf(entries.values()));
    }

    /** Read-only view DTO for ledger entries. */
    public record LedgerView(java.util.UUID childId, List<LedgerEntry> entries) {
        public LedgerView {
            Objects.requireNonNull(childId, "childId must not be null");
            Objects.requireNonNull(entries, "entries must not be null");
            entries = Collections.unmodifiableList(entries);
        }
    }
}

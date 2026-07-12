package com.jjt.platform.core.domain;

import com.jjt.platform.core.domain.entity.CoverageType;
import com.jjt.platform.core.domain.entity.EducationSupportLedger;
import com.jjt.platform.core.domain.entity.LedgerEntry;
import com.jjt.platform.core.domain.exceptions.LedgerInvariantViolationException;
import com.jjt.platform.core.domain.value.Money;
import com.jjt.platform.core.domain.value.YearMonthValue;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LedgerTest {

    private static final UUID CHILD_ID = UUID.randomUUID();
    private static final Money AMOUNT   = Money.of(new BigDecimal("2000.00"), Currency.getInstance("PKR"));
    private static final YearMonthValue JAN_2025 = YearMonthValue.of(2025, 1);
    private static final YearMonthValue FEB_2025 = YearMonthValue.of(2025, 2);

    private LedgerEntry entry(YearMonthValue month) {
        return new LedgerEntry(UUID.randomUUID(), CHILD_ID, month, AMOUNT,
                CoverageType.EARLY_SUPPORT, null, null);
    }

    @Test
    void newLedger_hasNoEntries() {
        EducationSupportLedger ledger = EducationSupportLedger.create(UUID.randomUUID(), CHILD_ID);
        assertTrue(ledger.getEntriesByMonth().isEmpty());
    }

    @Test
    void appendEntry_returnsNewLedgerWithEntry() {
        EducationSupportLedger empty  = EducationSupportLedger.create(UUID.randomUUID(), CHILD_ID);
        EducationSupportLedger filled = empty.appendEntry(entry(JAN_2025));

        assertTrue(filled.hasEntryFor(JAN_2025));
        assertFalse(empty.hasEntryFor(JAN_2025)); // original unchanged — immutable
    }

    @Test
    void appendEntry_allowsMultipleDistinctMonths() {
        EducationSupportLedger ledger = EducationSupportLedger.create(UUID.randomUUID(), CHILD_ID)
                .appendEntry(entry(JAN_2025))
                .appendEntry(entry(FEB_2025));

        assertTrue(ledger.hasEntryFor(JAN_2025));
        assertTrue(ledger.hasEntryFor(FEB_2025));
        assertEquals(2, ledger.getEntriesByMonth().size());
    }

    @Test
    void appendEntry_rejectsDuplicateMonth() {
        EducationSupportLedger ledger = EducationSupportLedger.create(UUID.randomUUID(), CHILD_ID)
                .appendEntry(entry(JAN_2025));

        assertThrows(LedgerInvariantViolationException.class,
                () -> ledger.appendEntry(entry(JAN_2025)));
    }

    @Test
    void appendEntry_rejectsEntryForWrongChild() {
        EducationSupportLedger ledger = EducationSupportLedger.create(UUID.randomUUID(), CHILD_ID);
        LedgerEntry wrongChild = new LedgerEntry(UUID.randomUUID(), UUID.randomUUID(),
                JAN_2025, AMOUNT, CoverageType.EARLY_SUPPORT, null, null);

        assertThrows(LedgerInvariantViolationException.class,
                () -> ledger.appendEntry(wrongChild));
    }

    @Test
    void appendEntry_rejectsNull() {
        EducationSupportLedger ledger = EducationSupportLedger.create(UUID.randomUUID(), CHILD_ID);
        assertThrows(NullPointerException.class, () -> ledger.appendEntry(null));
    }
}

package com.jjt.platform.application.usecase;

import com.jjt.platform.core.domain.entity.CoverageType;
import com.jjt.platform.core.domain.entity.EducationSupportLedger;
import com.jjt.platform.core.domain.entity.LedgerEntry;
import com.jjt.platform.core.domain.exceptions.DomainException;
import com.jjt.platform.core.domain.value.Money;
import com.jjt.platform.core.domain.value.YearMonthValue;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Use case: Record early support for a child by appending a ledger entry (append-only).
 */
public class RecordEarlySupportUseCase {

    public EducationSupportLedger record(Command command, EducationSupportLedger ledger) {
        Objects.requireNonNull(command, "command must not be null");
        Objects.requireNonNull(ledger, "ledger must not be null");

        if (!ledger.getChildId().equals(command.childId)) {
            throw new DomainException("Ledger childId does not match command childId");
        }

        LedgerEntry entry = new LedgerEntry(
                command.ledgerEntryId != null ? command.ledgerEntryId : UUID.randomUUID(),
                command.childId,
                command.month,
                command.educationCost,
                command.coverageType,
                command.createdBy,
                Instant.now()
        );

        return ledger.appendEntry(entry);
    }

    public record Command(UUID ledgerEntryId, UUID childId, YearMonthValue month, Money educationCost,
                          CoverageType coverageType, UUID createdBy) {
        public Command {
            Objects.requireNonNull(childId, "childId must not be null");
            Objects.requireNonNull(month, "month must not be null");
            Objects.requireNonNull(educationCost, "educationCost must not be null");
            Objects.requireNonNull(coverageType, "coverageType must not be null");
        }
    }
}

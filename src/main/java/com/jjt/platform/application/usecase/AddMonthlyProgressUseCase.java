package com.jjt.platform.application.usecase;

import com.jjt.platform.core.domain.entity.EducationSupportLedger;
import com.jjt.platform.core.domain.entity.ProgressUpdate;
import com.jjt.platform.core.domain.value.YearMonthValue;

import java.util.Objects;
import java.util.UUID;

/**
 * Use case: Add a monthly progress update aligned with an existing ledger month.
 */
public class AddMonthlyProgressUseCase {

    public ProgressUpdate add(Command command, EducationSupportLedger ledger) {
        Objects.requireNonNull(command, "command must not be null");
        Objects.requireNonNull(ledger, "ledger must not be null");
        UUID progressId = command.progressUpdateId != null ? command.progressUpdateId : UUID.randomUUID();
        return ProgressUpdate.create(progressId, command.childId, command.month, command.summary, ledger);
    }

    /** Input for adding a progress update. */
    public record Command(UUID progressUpdateId, UUID childId, YearMonthValue month, String summary) {
        public Command {
            Objects.requireNonNull(childId, "childId must not be null");
            Objects.requireNonNull(month, "month must not be null");
            Objects.requireNonNull(summary, "summary must not be null");
        }
    }
}

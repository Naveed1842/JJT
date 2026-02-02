package com.jjt.platform.application.usecase;

import com.jjt.platform.core.domain.entity.Sponsorship;
import com.jjt.platform.core.domain.value.YearMonthValue;

import java.util.Objects;
import java.util.UUID;

/**
 * Use case: Commit a sponsor to future support starting in a future month.
 */
public class CommitFutureSponsorshipUseCase {

    public Sponsorship commit(Command command) {
        Objects.requireNonNull(command, "command must not be null");
        UUID sponsorshipId = command.sponsorshipId != null ? command.sponsorshipId : UUID.randomUUID();
        return Sponsorship.create(sponsorshipId, command.sponsorId, command.childId, command.startMonth);
    }

    /** Input for creating a future sponsorship commitment. */
    public record Command(UUID sponsorshipId, UUID sponsorId, UUID childId, YearMonthValue startMonth) {
        public Command {
            Objects.requireNonNull(sponsorId, "sponsorId must not be null");
            Objects.requireNonNull(childId, "childId must not be null");
            Objects.requireNonNull(startMonth, "startMonth must not be null");
        }
    }
}

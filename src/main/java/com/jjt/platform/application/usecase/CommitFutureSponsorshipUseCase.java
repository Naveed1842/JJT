package com.jjt.platform.application.usecase;

import com.jjt.platform.core.domain.entity.CommitmentType;
import com.jjt.platform.core.domain.entity.Sponsorship;
import com.jjt.platform.core.domain.exceptions.SponsorshipInvariantViolationException;
import com.jjt.platform.core.domain.value.YearMonthValue;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Use case: Commit a sponsor to future support starting in a future month.
 * Always creates a PENDING sponsorship; use activateSponsorship to transition to ACTIVE.
 */
public class CommitFutureSponsorshipUseCase {

    public Sponsorship commit(Command command) {
        Objects.requireNonNull(command, "command must not be null");
        if (command.hasActiveSponsorship) {
            throw new SponsorshipInvariantViolationException(
                    "Child already has an active or pending sponsorship. Expire it before creating a new one.");
        }
        UUID sponsorshipId = command.sponsorshipId != null ? command.sponsorshipId : UUID.randomUUID();
        return Sponsorship.createPending(
                sponsorshipId,
                command.sponsorId,
                command.childId,
                command.startMonth,
                command.now,
                command.commitmentType,
                command.createdBy
        );
    }

    public record Command(UUID sponsorshipId,
                          UUID sponsorId,
                          UUID childId,
                          YearMonthValue startMonth,
                          boolean hasActiveSponsorship,
                          Instant now,
                          Instant expiresAt,
                          CommitmentType commitmentType,
                          UUID createdBy) {
        public Command {
            Objects.requireNonNull(sponsorId, "sponsorId must not be null");
            Objects.requireNonNull(childId, "childId must not be null");
            Objects.requireNonNull(startMonth, "startMonth must not be null");
            Objects.requireNonNull(commitmentType, "commitmentType must not be null");
        }
    }
}

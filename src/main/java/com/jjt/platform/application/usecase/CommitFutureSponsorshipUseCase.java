package com.jjt.platform.application.usecase;

import com.jjt.platform.core.domain.entity.Sponsorship;
import com.jjt.platform.core.domain.entity.SponsorshipStatus;
import com.jjt.platform.core.domain.exceptions.SponsorshipInvariantViolationException;
import com.jjt.platform.core.domain.value.YearMonthValue;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Use case: Commit a sponsor to future support starting in a future month.
 */
public class CommitFutureSponsorshipUseCase {

    public Sponsorship commit(Command command) {
        Objects.requireNonNull(command, "command must not be null");
        if (command.hasActiveSponsorship) {
            throw new SponsorshipInvariantViolationException("Child already has an active sponsorship.");
        }
        UUID sponsorshipId = command.sponsorshipId != null ? command.sponsorshipId : UUID.randomUUID();
        Instant createdAt = command.createdAt != null ? command.createdAt : Instant.now();
        return Sponsorship.create(sponsorshipId, command.sponsorId, command.childId, command.startMonth,
                command.status, createdAt, command.expiresAt);
    }

    /** Input for creating a future sponsorship commitment. */
    public record Command(UUID sponsorshipId,
                          UUID sponsorId,
                          UUID childId,
                          YearMonthValue startMonth,
                          boolean hasActiveSponsorship,
                          SponsorshipStatus status,
                          Instant createdAt,
                          Instant expiresAt) {
        public Command {
            Objects.requireNonNull(sponsorId, "sponsorId must not be null");
            Objects.requireNonNull(childId, "childId must not be null");
            Objects.requireNonNull(startMonth, "startMonth must not be null");
            Objects.requireNonNull(status, "status must not be null");
        }
    }
}

package com.jjt.platform.application.usecase;

import com.jjt.platform.core.domain.entity.Sponsor;

import java.util.Objects;
import java.util.UUID;

/**
 * Use case: Register a sponsor who can commit to future sponsorships.
 */
public class CreateSponsorUseCase {

    public Sponsor create(Command command) {
        Objects.requireNonNull(command, "command must not be null");
        UUID sponsorId = command.sponsorId != null ? command.sponsorId : UUID.randomUUID();
        
        return new Sponsor(sponsorId, command.displayName, command.contactEmail, command.phone);
    }

    /** Input data for creating a sponsor. */
    public record Command(UUID sponsorId, String displayName, String contactEmail, String phone) {
        public Command {
            Objects.requireNonNull(displayName, "displayName must not be null");
            Objects.requireNonNull(contactEmail, "contactEmail must not be null");
        }
    }
}

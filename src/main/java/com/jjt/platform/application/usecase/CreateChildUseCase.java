package com.jjt.platform.application.usecase;

import com.jjt.platform.core.domain.entity.Child;
import com.jjt.platform.core.domain.entity.EducationSupportLedger;
import com.jjt.platform.core.domain.value.Money;

import java.util.Objects;
import java.util.UUID;

/**
 * Use case: Register a child without requiring a sponsor and open the child's ledger.
 * Returns both the new Child and its empty EducationSupportLedger.
 */
public class CreateChildUseCase {

    public Result create(Command command) {
        Objects.requireNonNull(command, "command must not be null");
        UUID childId = command.childId != null ? command.childId : UUID.randomUUID();
        UUID ledgerId = command.ledgerId != null ? command.ledgerId : UUID.randomUUID();

        Child child = new Child(
                childId,
                command.fullName,
                command.educationCost,
                command.rollNumber,
                command.city,
                command.campusName,
                command.schoolName);
        EducationSupportLedger ledger = EducationSupportLedger.create(ledgerId, childId);

        return new Result(child, ledger);
    }

    /** Input data for creating a child and ledger. */
    public record Command(UUID childId, UUID ledgerId, String fullName, Money educationCost,
                          String rollNumber, String city, String campusName, String schoolName) {
        public Command {
            Objects.requireNonNull(fullName, "fullName must not be null");
            Objects.requireNonNull(educationCost, "educationCost must not be null");
            Objects.requireNonNull(rollNumber, "rollNumber must not be null");
            Objects.requireNonNull(city, "city must not be null");
            Objects.requireNonNull(campusName, "campusName must not be null");
            if (rollNumber.isBlank()) throw new IllegalArgumentException("rollNumber must not be blank");
            if (city.isBlank()) throw new IllegalArgumentException("city must not be blank");
            if (campusName.isBlank()) throw new IllegalArgumentException("campusName must not be blank");
        }
    }

    /** Output data containing both child and its ledger. */
    public record Result(Child child, EducationSupportLedger ledger) {
        public Result {
            Objects.requireNonNull(child, "child must not be null");
            Objects.requireNonNull(ledger, "ledger must not be null");
        }
    }
}

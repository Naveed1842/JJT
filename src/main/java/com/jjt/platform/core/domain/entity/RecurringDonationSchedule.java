package com.jjt.platform.core.domain.entity;

import com.jjt.platform.core.domain.value.Money;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public final class RecurringDonationSchedule {

    private final UUID id;
    private final UUID organisationId;
    private final UUID donorId;
    private final DonationType donationType;
    private final Money amount;
    private final DonationFrequency frequency;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final LocalDate nextDueDate;
    private final RecurringDonationStatus status;
    private final UUID fundAccountId;
    private final String notes;
    private final UUID createdBy;
    private final Instant createdAt;

    public RecurringDonationSchedule(UUID id, UUID organisationId, UUID donorId,
                                     DonationType donationType, Money amount,
                                     DonationFrequency frequency, LocalDate startDate,
                                     LocalDate endDate, LocalDate nextDueDate,
                                     RecurringDonationStatus status, UUID fundAccountId,
                                     String notes, UUID createdBy, Instant createdAt) {
        this.id = id;
        this.organisationId = organisationId;
        this.donorId = donorId;
        this.donationType = donationType;
        this.amount = amount;
        this.frequency = frequency;
        this.startDate = startDate;
        this.endDate = endDate;
        this.nextDueDate = nextDueDate;
        this.status = status;
        this.fundAccountId = fundAccountId;
        this.notes = notes;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public static RecurringDonationSchedule createNew(UUID organisationId, UUID donorId,
                                                      DonationType donationType, Money amount,
                                                      DonationFrequency frequency, LocalDate startDate,
                                                      LocalDate endDate, UUID fundAccountId,
                                                      String notes, UUID createdBy) {
        return new RecurringDonationSchedule(UUID.randomUUID(), organisationId, donorId,
                donationType, amount, frequency, startDate, endDate, startDate,
                RecurringDonationStatus.ACTIVE, fundAccountId, notes, createdBy, Instant.now());
    }

    public RecurringDonationSchedule withNextDueDate(LocalDate nextDueDate) {
        return new RecurringDonationSchedule(id, organisationId, donorId, donationType, amount,
                frequency, startDate, endDate, nextDueDate, status, fundAccountId, notes, createdBy, createdAt);
    }

    public RecurringDonationSchedule withStatus(RecurringDonationStatus newStatus) {
        return new RecurringDonationSchedule(id, organisationId, donorId, donationType, amount,
                frequency, startDate, endDate, nextDueDate, newStatus, fundAccountId, notes, createdBy, createdAt);
    }

    public UUID getId() { return id; }
    public UUID getOrganisationId() { return organisationId; }
    public UUID getDonorId() { return donorId; }
    public DonationType getDonationType() { return donationType; }
    public Money getAmount() { return amount; }
    public DonationFrequency getFrequency() { return frequency; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public LocalDate getNextDueDate() { return nextDueDate; }
    public RecurringDonationStatus getStatus() { return status; }
    public UUID getFundAccountId() { return fundAccountId; }
    public String getNotes() { return notes; }
    public UUID getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }
}

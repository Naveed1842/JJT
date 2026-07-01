package com.jjt.platform.core.domain.entity;

import com.jjt.platform.core.domain.value.Money;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public final class Donation {

    private final UUID id;
    private final UUID organisationId;
    private final UUID donorId;
    private final DonationType donationType;
    private final Money amount;
    private final LocalDate donationDate;
    private final String receiptNumber;
    private final String notes;
    private final UUID fundAccountId;
    private final UUID fundTransactionId;
    private final DonationStatus status;
    private final UUID recurringScheduleId;
    private final UUID createdBy;
    private final Instant createdAt;
    private final UUID updatedBy;
    private final Instant updatedAt;

    public Donation(UUID id, UUID organisationId, UUID donorId, DonationType donationType,
                    Money amount, LocalDate donationDate, String receiptNumber, String notes,
                    UUID fundAccountId, UUID fundTransactionId, DonationStatus status,
                    UUID recurringScheduleId, UUID createdBy, Instant createdAt,
                    UUID updatedBy, Instant updatedAt) {
        this.id = id;
        this.organisationId = organisationId;
        this.donorId = donorId;
        this.donationType = donationType;
        this.amount = amount;
        this.donationDate = donationDate;
        this.receiptNumber = receiptNumber;
        this.notes = notes;
        this.fundAccountId = fundAccountId;
        this.fundTransactionId = fundTransactionId;
        this.status = status;
        this.recurringScheduleId = recurringScheduleId;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }

    public static Donation createNew(UUID organisationId, UUID donorId, DonationType donationType,
                                     Money amount, LocalDate donationDate, String receiptNumber,
                                     String notes, UUID fundAccountId, UUID fundTransactionId,
                                     UUID createdBy) {
        Instant now = Instant.now();
        return new Donation(UUID.randomUUID(), organisationId, donorId, donationType, amount,
                donationDate, receiptNumber, notes, fundAccountId, fundTransactionId,
                DonationStatus.RECEIPTED, null, createdBy, now, createdBy, now);
    }

    public static Donation createExpected(UUID organisationId, UUID donorId, DonationType donationType,
                                          Money amount, LocalDate donationDate, String notes,
                                          UUID fundAccountId, UUID recurringScheduleId, UUID createdBy) {
        Instant now = Instant.now();
        return new Donation(UUID.randomUUID(), organisationId, donorId, donationType, amount,
                donationDate, null, notes, fundAccountId, null,
                DonationStatus.EXPECTED, recurringScheduleId, createdBy, now, createdBy, now);
    }

    public Donation receiveAndReceipt(String receiptNumber, UUID fundTransactionId, UUID updatedBy) {
        return new Donation(id, organisationId, donorId, donationType, amount, donationDate,
                receiptNumber, notes, fundAccountId, fundTransactionId,
                DonationStatus.RECEIPTED, recurringScheduleId, createdBy, createdAt, updatedBy, Instant.now());
    }

    public Donation reverse(UUID updatedBy) {
        return new Donation(id, organisationId, donorId, donationType, amount, donationDate,
                receiptNumber, notes, fundAccountId, fundTransactionId,
                DonationStatus.REVERSED, recurringScheduleId, createdBy, createdAt, updatedBy, Instant.now());
    }

    public UUID getId() { return id; }
    public UUID getOrganisationId() { return organisationId; }
    public UUID getDonorId() { return donorId; }
    public DonationType getDonationType() { return donationType; }
    public Money getAmount() { return amount; }
    public LocalDate getDonationDate() { return donationDate; }
    public String getReceiptNumber() { return receiptNumber; }
    public String getNotes() { return notes; }
    public UUID getFundAccountId() { return fundAccountId; }
    public UUID getFundTransactionId() { return fundTransactionId; }
    public DonationStatus getStatus() { return status; }
    public UUID getRecurringScheduleId() { return recurringScheduleId; }
    public UUID getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public UUID getUpdatedBy() { return updatedBy; }
    public Instant getUpdatedAt() { return updatedAt; }
}

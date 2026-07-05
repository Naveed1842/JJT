package com.jjt.platform.core.domain.entity;

import com.jjt.platform.core.domain.value.Money;
import com.jjt.platform.core.domain.value.YearMonthValue;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public final class SponsorPayment {

    private final UUID id;
    private final UUID sponsorshipId;
    private final UUID sponsorId;
    private final UUID childId;
    private final YearMonthValue paymentMonth;
    private final SponsorPaymentStatus status;
    private final Money expectedAmount;
    private final Money receivedAmount;     // nullable until received
    private final String bankReference;     // nullable
    private final LocalDate receivedDate;   // nullable
    private final String waiverReason;      // nullable
    private final UUID fundTransactionId;   // nullable
    private final UUID ledgerEntryId;       // nullable
    private final UUID createdBy;
    private final Instant createdAt;
    private final UUID updatedBy;
    private final Instant updatedAt;

    private SponsorPayment(UUID id, UUID sponsorshipId, UUID sponsorId, UUID childId,
                           YearMonthValue paymentMonth, SponsorPaymentStatus status,
                           Money expectedAmount, Money receivedAmount,
                           String bankReference, LocalDate receivedDate, String waiverReason,
                           UUID fundTransactionId, UUID ledgerEntryId,
                           UUID createdBy, Instant createdAt, UUID updatedBy, Instant updatedAt) {
        this.id = Objects.requireNonNull(id);
        this.sponsorshipId = Objects.requireNonNull(sponsorshipId);
        this.sponsorId = Objects.requireNonNull(sponsorId);
        this.childId = Objects.requireNonNull(childId);
        this.paymentMonth = Objects.requireNonNull(paymentMonth);
        this.status = Objects.requireNonNull(status);
        this.expectedAmount = Objects.requireNonNull(expectedAmount);
        this.receivedAmount = receivedAmount;
        this.bankReference = bankReference;
        this.receivedDate = receivedDate;
        this.waiverReason = waiverReason;
        this.fundTransactionId = fundTransactionId;
        this.ledgerEntryId = ledgerEntryId;
        this.createdBy = createdBy;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedBy = updatedBy;
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static SponsorPayment restore(UUID id, UUID sponsorshipId, UUID sponsorId, UUID childId,
                                         YearMonthValue paymentMonth, SponsorPaymentStatus status,
                                         Money expectedAmount, Money receivedAmount,
                                         String bankReference, LocalDate receivedDate, String waiverReason,
                                         UUID fundTransactionId, UUID ledgerEntryId,
                                         UUID createdBy, Instant createdAt, UUID updatedBy, Instant updatedAt) {
        return new SponsorPayment(id, sponsorshipId, sponsorId, childId, paymentMonth, status,
                expectedAmount, receivedAmount, bankReference, receivedDate, waiverReason,
                fundTransactionId, ledgerEntryId, createdBy, createdAt, updatedBy, updatedAt);
    }

    public static SponsorPayment createExpected(UUID id, UUID sponsorshipId, UUID sponsorId, UUID childId,
                                                YearMonthValue paymentMonth, Money expectedAmount,
                                                UUID createdBy, Instant createdAt) {
        return new SponsorPayment(id, sponsorshipId, sponsorId, childId, paymentMonth,
                SponsorPaymentStatus.EXPECTED, expectedAmount,
                null, null, null, null, null, null,
                createdBy, createdAt, createdBy, createdAt);
    }

    public SponsorPayment markReceived(Money receivedAmount, String bankReference,
                                       LocalDate receivedDate, UUID fundTransactionId,
                                       UUID ledgerEntryId, UUID updatedBy, Instant updatedAt) {
        SponsorPaymentStatus newStatus = receivedAmount.getAmount()
                .compareTo(expectedAmount.getAmount()) >= 0
                ? SponsorPaymentStatus.RECEIVED
                : SponsorPaymentStatus.PARTIAL;
        return new SponsorPayment(id, sponsorshipId, sponsorId, childId, paymentMonth,
                newStatus, expectedAmount, receivedAmount,
                bankReference, receivedDate, null, fundTransactionId, ledgerEntryId,
                createdBy, createdAt, updatedBy, updatedAt);
    }

    public SponsorPayment markOverdue(UUID updatedBy, Instant updatedAt) {
        return new SponsorPayment(id, sponsorshipId, sponsorId, childId, paymentMonth,
                SponsorPaymentStatus.OVERDUE, expectedAmount, null,
                null, null, null, null, null,
                createdBy, createdAt, updatedBy, updatedAt);
    }

    public SponsorPayment waive(String reason, UUID ledgerEntryId, UUID updatedBy, Instant updatedAt) {
        return new SponsorPayment(id, sponsorshipId, sponsorId, childId, paymentMonth,
                SponsorPaymentStatus.WAIVED, expectedAmount, null,
                null, null, reason, null, ledgerEntryId,
                createdBy, createdAt, updatedBy, updatedAt);
    }

    public UUID getId() { return id; }
    public UUID getSponsorshipId() { return sponsorshipId; }
    public UUID getSponsorId() { return sponsorId; }
    public UUID getChildId() { return childId; }
    public YearMonthValue getPaymentMonth() { return paymentMonth; }
    public SponsorPaymentStatus getStatus() { return status; }
    public Money getExpectedAmount() { return expectedAmount; }
    public Money getReceivedAmount() { return receivedAmount; }
    public String getBankReference() { return bankReference; }
    public LocalDate getReceivedDate() { return receivedDate; }
    public String getWaiverReason() { return waiverReason; }
    public UUID getFundTransactionId() { return fundTransactionId; }
    public UUID getLedgerEntryId() { return ledgerEntryId; }
    public UUID getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public UUID getUpdatedBy() { return updatedBy; }
    public Instant getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return id.equals(((SponsorPayment) o).id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }
}

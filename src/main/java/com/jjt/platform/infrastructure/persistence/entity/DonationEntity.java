package com.jjt.platform.infrastructure.persistence.entity;

import com.jjt.platform.core.domain.entity.DonationStatus;
import com.jjt.platform.core.domain.entity.DonationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "donations")
public class DonationEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "organisation_id", nullable = false, updatable = false)
    private UUID organisationId;

    @Column(name = "donor_id")
    private UUID donorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "donation_type", nullable = false, length = 30)
    private DonationType donationType;

    @Column(name = "amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "donation_date", nullable = false)
    private LocalDate donationDate;

    @Column(name = "receipt_number", length = 30)
    private String receiptNumber;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "fund_account_id")
    private UUID fundAccountId;

    @Column(name = "fund_transaction_id")
    private UUID fundTransactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DonationStatus status;

    @Column(name = "recurring_schedule_id")
    private UUID recurringScheduleId;

    @Column(name = "created_by", updatable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected DonationEntity() {}

    public DonationEntity(UUID id, UUID organisationId, UUID donorId, DonationType donationType,
                          BigDecimal amount, String currency, LocalDate donationDate,
                          String receiptNumber, String notes, UUID fundAccountId,
                          UUID fundTransactionId, DonationStatus status, UUID recurringScheduleId,
                          UUID createdBy, Instant createdAt, UUID updatedBy, Instant updatedAt) {
        this.id = id;
        this.organisationId = organisationId;
        this.donorId = donorId;
        this.donationType = donationType;
        this.amount = amount;
        this.currency = currency;
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

    public UUID getId() { return id; }
    public UUID getOrganisationId() { return organisationId; }
    public UUID getDonorId() { return donorId; }
    public DonationType getDonationType() { return donationType; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
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

    public void setStatus(DonationStatus status) { this.status = status; }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }
    public void setFundTransactionId(UUID fundTransactionId) { this.fundTransactionId = fundTransactionId; }
    public void setUpdatedBy(UUID updatedBy) { this.updatedBy = updatedBy; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

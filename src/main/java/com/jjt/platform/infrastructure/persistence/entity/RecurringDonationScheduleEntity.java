package com.jjt.platform.infrastructure.persistence.entity;

import com.jjt.platform.core.domain.entity.DonationFrequency;
import com.jjt.platform.core.domain.entity.DonationType;
import com.jjt.platform.core.domain.entity.RecurringDonationStatus;
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
@Table(name = "recurring_donation_schedules")
public class RecurringDonationScheduleEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "organisation_id", nullable = false, updatable = false)
    private UUID organisationId;

    @Column(name = "donor_id", nullable = false, updatable = false)
    private UUID donorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "donation_type", nullable = false, length = 30)
    private DonationType donationType;

    @Column(name = "amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", nullable = false, length = 20)
    private DonationFrequency frequency;

    @Column(name = "start_date", nullable = false, updatable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "next_due_date", nullable = false)
    private LocalDate nextDueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RecurringDonationStatus status;

    @Column(name = "fund_account_id")
    private UUID fundAccountId;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_by", updatable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected RecurringDonationScheduleEntity() {}

    public RecurringDonationScheduleEntity(UUID id, UUID organisationId, UUID donorId,
                                           DonationType donationType, BigDecimal amount, String currency,
                                           DonationFrequency frequency, LocalDate startDate,
                                           LocalDate endDate, LocalDate nextDueDate,
                                           RecurringDonationStatus status, UUID fundAccountId,
                                           String notes, UUID createdBy, Instant createdAt) {
        this.id = id;
        this.organisationId = organisationId;
        this.donorId = donorId;
        this.donationType = donationType;
        this.amount = amount;
        this.currency = currency;
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

    public UUID getId() { return id; }
    public UUID getOrganisationId() { return organisationId; }
    public UUID getDonorId() { return donorId; }
    public DonationType getDonationType() { return donationType; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public DonationFrequency getFrequency() { return frequency; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public LocalDate getNextDueDate() { return nextDueDate; }
    public RecurringDonationStatus getStatus() { return status; }
    public UUID getFundAccountId() { return fundAccountId; }
    public String getNotes() { return notes; }
    public UUID getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }

    public void setNextDueDate(LocalDate nextDueDate) { this.nextDueDate = nextDueDate; }
    public void setStatus(RecurringDonationStatus status) { this.status = status; }
}

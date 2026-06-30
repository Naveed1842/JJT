package com.jjt.platform.infrastructure.persistence.entity;

import com.jjt.platform.core.domain.entity.SponsorPaymentStatus;
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
@Table(name = "sponsor_payments")
public class SponsorPaymentEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "sponsorship_id", nullable = false, updatable = false)
    private UUID sponsorshipId;

    @Column(name = "sponsor_id", nullable = false, updatable = false)
    private UUID sponsorId;

    @Column(name = "child_id", nullable = false, updatable = false)
    private UUID childId;

    @Column(name = "payment_month", nullable = false, length = 7, updatable = false)
    private String paymentMonth;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private SponsorPaymentStatus status;

    @Column(name = "expected_amount", nullable = false, precision = 14, scale = 2, updatable = false)
    private BigDecimal expectedAmount;

    @Column(name = "expected_currency", nullable = false, length = 3, updatable = false)
    private String expectedCurrency;

    @Column(name = "received_amount", precision = 14, scale = 2)
    private BigDecimal receivedAmount;

    @Column(name = "received_currency", length = 3)
    private String receivedCurrency;

    @Column(name = "bank_reference", length = 200)
    private String bankReference;

    @Column(name = "received_date")
    private LocalDate receivedDate;

    @Column(name = "waiver_reason")
    private String waiverReason;

    @Column(name = "fund_transaction_id")
    private UUID fundTransactionId;

    @Column(name = "ledger_entry_id")
    private UUID ledgerEntryId;

    @Column(name = "created_by", updatable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected SponsorPaymentEntity() {}

    public SponsorPaymentEntity(UUID id, UUID sponsorshipId, UUID sponsorId, UUID childId,
                                String paymentMonth, SponsorPaymentStatus status,
                                BigDecimal expectedAmount, String expectedCurrency,
                                BigDecimal receivedAmount, String receivedCurrency,
                                String bankReference, LocalDate receivedDate, String waiverReason,
                                UUID fundTransactionId, UUID ledgerEntryId,
                                UUID createdBy, Instant createdAt, UUID updatedBy, Instant updatedAt) {
        this.id = id;
        this.sponsorshipId = sponsorshipId;
        this.sponsorId = sponsorId;
        this.childId = childId;
        this.paymentMonth = paymentMonth;
        this.status = status;
        this.expectedAmount = expectedAmount;
        this.expectedCurrency = expectedCurrency;
        this.receivedAmount = receivedAmount;
        this.receivedCurrency = receivedCurrency;
        this.bankReference = bankReference;
        this.receivedDate = receivedDate;
        this.waiverReason = waiverReason;
        this.fundTransactionId = fundTransactionId;
        this.ledgerEntryId = ledgerEntryId;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public UUID getSponsorshipId() { return sponsorshipId; }
    public UUID getSponsorId() { return sponsorId; }
    public UUID getChildId() { return childId; }
    public String getPaymentMonth() { return paymentMonth; }
    public SponsorPaymentStatus getStatus() { return status; }
    public BigDecimal getExpectedAmount() { return expectedAmount; }
    public String getExpectedCurrency() { return expectedCurrency; }
    public BigDecimal getReceivedAmount() { return receivedAmount; }
    public String getReceivedCurrency() { return receivedCurrency; }
    public String getBankReference() { return bankReference; }
    public LocalDate getReceivedDate() { return receivedDate; }
    public String getWaiverReason() { return waiverReason; }
    public UUID getFundTransactionId() { return fundTransactionId; }
    public UUID getLedgerEntryId() { return ledgerEntryId; }
    public UUID getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public UUID getUpdatedBy() { return updatedBy; }
    public Instant getUpdatedAt() { return updatedAt; }
}

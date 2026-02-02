package com.jjt.platform.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "ledger_entries",
       uniqueConstraints = @UniqueConstraint(name = "uk_ledger_month", columnNames = {"ledger_id", "entry_month"}))
@Immutable
public class LedgerEntryEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ledger_id", nullable = false)
    private EducationSupportLedgerEntity ledger;

    @Column(name = "child_id", nullable = false, updatable = false)
    private UUID childId;

    @Column(name = "entry_month", nullable = false, length = 7, updatable = false)
    private String entryMonth; // YYYY-MM

    @Column(name = "education_amount", nullable = false, precision = 12, scale = 2, updatable = false)
    private BigDecimal educationAmount;

    @Column(name = "education_currency", nullable = false, length = 3, updatable = false)
    private String educationCurrency;

    protected LedgerEntryEntity() {
    }

    public LedgerEntryEntity(UUID id, EducationSupportLedgerEntity ledger, UUID childId,
                              String entryMonth, BigDecimal educationAmount, String educationCurrency) {
        this.id = id;
        this.ledger = ledger;
        this.childId = childId;
        this.entryMonth = entryMonth;
        this.educationAmount = educationAmount;
        this.educationCurrency = educationCurrency;
    }

    public UUID getId() {
        return id;
    }

    public EducationSupportLedgerEntity getLedger() {
        return ledger;
    }

    public UUID getChildId() {
        return childId;
    }

    public String getEntryMonth() {
        return entryMonth;
    }

    public BigDecimal getEducationAmount() {
        return educationAmount;
    }

    public String getEducationCurrency() {
        return educationCurrency;
    }
}

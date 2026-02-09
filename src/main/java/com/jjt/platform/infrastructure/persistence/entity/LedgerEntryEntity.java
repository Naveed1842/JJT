package com.jjt.platform.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "ledger_entries",
       uniqueConstraints = @UniqueConstraint(name = "uk_ledger_month", columnNames = {"ledger_id", "entry_month"}))
@Immutable
@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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
    
    // Validation method using Apache Commons
    public static LedgerEntryEntity create(UUID id, EducationSupportLedgerEntity ledger, UUID childId,
                                           String entryMonth, BigDecimal educationAmount, String educationCurrency) {
        Validate.notNull(id, "Ledger entry ID cannot be null");
        Validate.notNull(ledger, "Ledger cannot be null");
        Validate.notNull(childId, "Child ID cannot be null");
        Validate.isTrue(StringUtils.isNotBlank(entryMonth), "Entry month cannot be blank");
        Validate.notNull(educationAmount, "Education amount cannot be null");
        Validate.isTrue(educationAmount.compareTo(BigDecimal.ZERO) > 0, "Education amount must be positive");
        Validate.isTrue(StringUtils.isNotBlank(educationCurrency), "Education currency cannot be blank");
        
        return LedgerEntryEntity.builder()
                .id(id)
                .ledger(ledger)
                .childId(childId)
                .entryMonth(StringUtils.trim(entryMonth))
                .educationAmount(educationAmount)
                .educationCurrency(StringUtils.upperCase(StringUtils.trim(educationCurrency)))
                .build();
    }
}

package com.jjt.platform.infrastructure.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.Validate;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "education_support_ledgers")
@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EducationSupportLedgerEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @OneToOne(optional = false)
    @JoinColumn(name = "child_id", nullable = false, unique = true)
    private ChildEntity child;

    @OneToMany(mappedBy = "ledger", cascade = CascadeType.ALL, orphanRemoval = false)
    private Set<LedgerEntryEntity> entries = new LinkedHashSet<>();
    
    // Validation method using Apache Commons
    public static EducationSupportLedgerEntity create(UUID id, ChildEntity child) {
        Validate.notNull(id, "Ledger ID cannot be null");
        Validate.notNull(child, "Child cannot be null");
        
        return EducationSupportLedgerEntity.builder()
                .id(id)
                .child(child)
                .entries(new LinkedHashSet<>())
                .build();
    }
}

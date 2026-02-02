package com.jjt.platform.infrastructure.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "education_support_ledgers")
public class EducationSupportLedgerEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @OneToOne(optional = false)
    @JoinColumn(name = "child_id", nullable = false, unique = true)
    private ChildEntity child;

    @OneToMany(mappedBy = "ledger", cascade = CascadeType.ALL, orphanRemoval = false)
    private Set<LedgerEntryEntity> entries = new LinkedHashSet<>();

    protected EducationSupportLedgerEntity() {
    }

    public EducationSupportLedgerEntity(UUID id, ChildEntity child) {
        this.id = id;
        this.child = child;
    }

    public UUID getId() {
        return id;
    }

    public ChildEntity getChild() {
        return child;
    }

    public Set<LedgerEntryEntity> getEntries() {
        return entries;
    }
}

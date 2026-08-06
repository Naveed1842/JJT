package com.jjt.platform.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transparency_snapshots")
public class TransparencySnapshotEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "org_id", nullable = false)
    private UUID orgId;

    @Column(name = "period_id", nullable = false)
    private UUID periodId;

    @Column(name = "computed_at", nullable = false)
    private Instant computedAt;

    @Column(name = "programme_pct", precision = 5, scale = 2)
    private BigDecimal programmePct;

    @Column(name = "admin_pct", precision = 5, scale = 2)
    private BigDecimal adminPct;

    @Column(name = "fundraising_pct", precision = 5, scale = 2)
    private BigDecimal fundraisingPct;

    @Column(name = "total_income", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalIncome;

    @Column(name = "total_expense", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalExpense;

    @Column(name = "beneficiary_count", nullable = false)
    private int beneficiaryCount;

    @Column(name = "cost_per_beneficiary", precision = 10, scale = 2)
    private BigDecimal costPerBeneficiary;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "per_programme", columnDefinition = "jsonb")
    private String perProgramme;

    @Column(name = "published", nullable = false)
    private boolean published;

    protected TransparencySnapshotEntity() {}

    public TransparencySnapshotEntity(UUID id, UUID orgId, UUID periodId) {
        this.id = id; this.orgId = orgId; this.periodId = periodId;
        this.computedAt = Instant.now(); this.totalIncome = BigDecimal.ZERO;
        this.totalExpense = BigDecimal.ZERO; this.beneficiaryCount = 0;
        this.perProgramme = "[]"; this.published = false;
    }

    public UUID getId() { return id; }
    public UUID getOrgId() { return orgId; }
    public UUID getPeriodId() { return periodId; }
    public Instant getComputedAt() { return computedAt; }
    public void setComputedAt(Instant computedAt) { this.computedAt = computedAt; }
    public BigDecimal getProgrammePct() { return programmePct; }
    public void setProgrammePct(BigDecimal programmePct) { this.programmePct = programmePct; }
    public BigDecimal getAdminPct() { return adminPct; }
    public void setAdminPct(BigDecimal adminPct) { this.adminPct = adminPct; }
    public BigDecimal getFundraisingPct() { return fundraisingPct; }
    public void setFundraisingPct(BigDecimal fundraisingPct) { this.fundraisingPct = fundraisingPct; }
    public BigDecimal getTotalIncome() { return totalIncome; }
    public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }
    public BigDecimal getTotalExpense() { return totalExpense; }
    public void setTotalExpense(BigDecimal totalExpense) { this.totalExpense = totalExpense; }
    public int getBeneficiaryCount() { return beneficiaryCount; }
    public void setBeneficiaryCount(int beneficiaryCount) { this.beneficiaryCount = beneficiaryCount; }
    public BigDecimal getCostPerBeneficiary() { return costPerBeneficiary; }
    public void setCostPerBeneficiary(BigDecimal costPerBeneficiary) { this.costPerBeneficiary = costPerBeneficiary; }
    public String getPerProgramme() { return perProgramme; }
    public void setPerProgramme(String perProgramme) { this.perProgramme = perProgramme; }
    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }
}

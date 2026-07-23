package com.jjt.platform.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "payroll_runs")
public class PayrollRunEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "org_id", nullable = false)
    private UUID orgId;

    @Column(name = "period_id", nullable = false)
    private UUID periodId;

    @Column(name = "run_date", nullable = false)
    private LocalDate runDate;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "total_gross", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalGross;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected PayrollRunEntity() {}

    public PayrollRunEntity(UUID id, UUID orgId, UUID periodId, LocalDate runDate, UUID createdBy) {
        this.id = id; this.orgId = orgId; this.periodId = periodId;
        this.runDate = runDate; this.status = "DRAFT";
        this.totalGross = BigDecimal.ZERO;
        this.createdBy = createdBy; this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getOrgId() { return orgId; }
    public UUID getPeriodId() { return periodId; }
    public LocalDate getRunDate() { return runDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getTotalGross() { return totalGross; }
    public void setTotalGross(BigDecimal totalGross) { this.totalGross = totalGross; }
    public UUID getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }
}

package com.jjt.platform.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "financial_periods")
public class FinancialPeriodEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "org_id", nullable = false)
    private UUID orgId;

    @Column(name = "label", nullable = false, length = 50)
    private String label;

    @Column(name = "period_type", nullable = false, length = 20)
    private String periodType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "closed_by")
    private UUID closedBy;

    protected FinancialPeriodEntity() {}

    public FinancialPeriodEntity(UUID id, UUID orgId, String label, String periodType,
                                 LocalDate startDate, LocalDate endDate) {
        this.id = id; this.orgId = orgId; this.label = label;
        this.periodType = periodType; this.startDate = startDate;
        this.endDate = endDate; this.status = "OPEN";
    }

    public UUID getId() { return id; }
    public UUID getOrgId() { return orgId; }
    public String getLabel() { return label; }
    public String getPeriodType() { return periodType; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getClosedAt() { return closedAt; }
    public void setClosedAt(Instant closedAt) { this.closedAt = closedAt; }
    public UUID getClosedBy() { return closedBy; }
    public void setClosedBy(UUID closedBy) { this.closedBy = closedBy; }
}

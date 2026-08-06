package com.jjt.platform.core.domain.entity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public final class FinancialPeriod {
    private final UUID id;
    private final UUID orgId;
    private final String label;
    private final FinancialPeriodType periodType;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final FinancialPeriodStatus status;
    private final Instant closedAt;
    private final UUID closedBy;

    public FinancialPeriod(UUID id, UUID orgId, String label, FinancialPeriodType periodType,
                           LocalDate startDate, LocalDate endDate, FinancialPeriodStatus status,
                           Instant closedAt, UUID closedBy) {
        this.id = id; this.orgId = orgId; this.label = label;
        this.periodType = periodType; this.startDate = startDate;
        this.endDate = endDate; this.status = status;
        this.closedAt = closedAt; this.closedBy = closedBy;
    }

    public UUID getId() { return id; }
    public UUID getOrgId() { return orgId; }
    public String getLabel() { return label; }
    public FinancialPeriodType getPeriodType() { return periodType; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public FinancialPeriodStatus getStatus() { return status; }
    public Instant getClosedAt() { return closedAt; }
    public UUID getClosedBy() { return closedBy; }

    public FinancialPeriod withStatus(FinancialPeriodStatus newStatus, UUID actor) {
        return new FinancialPeriod(id, orgId, label, periodType, startDate, endDate,
                newStatus, Instant.now(), actor);
    }
}

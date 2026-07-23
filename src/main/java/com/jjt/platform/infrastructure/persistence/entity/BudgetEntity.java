package com.jjt.platform.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "budgets")
public class BudgetEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "org_id", nullable = false)
    private UUID orgId;

    @Column(name = "period_id", nullable = false)
    private UUID periodId;

    @Column(name = "category_id")
    private Integer categoryId;

    @Column(name = "cost_centre_id")
    private UUID costCentreId;

    @Column(name = "mission_node_id")
    private UUID missionNodeId;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    protected BudgetEntity() {}

    public BudgetEntity(UUID id, UUID orgId, UUID periodId, Integer categoryId,
                        UUID costCentreId, UUID missionNodeId, BigDecimal amount, String notes) {
        this.id = id; this.orgId = orgId; this.periodId = periodId;
        this.categoryId = categoryId; this.costCentreId = costCentreId;
        this.missionNodeId = missionNodeId; this.amount = amount; this.notes = notes;
    }

    public UUID getId() { return id; }
    public UUID getOrgId() { return orgId; }
    public UUID getPeriodId() { return periodId; }
    public Integer getCategoryId() { return categoryId; }
    public UUID getCostCentreId() { return costCentreId; }
    public UUID getMissionNodeId() { return missionNodeId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}

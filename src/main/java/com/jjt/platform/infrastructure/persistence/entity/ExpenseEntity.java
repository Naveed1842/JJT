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
@Table(name = "expenses")
public class ExpenseEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "org_id", nullable = false)
    private UUID orgId;

    @Column(name = "vendor_id")
    private UUID vendorId;

    @Column(name = "payee_text", length = 255)
    private String payeeText;

    @Column(name = "category_id")
    private Integer categoryId;

    @Column(name = "cost_centre_id")
    private UUID costCentreId;

    @Column(name = "mission_node_id")
    private UUID missionNodeId;

    @Column(name = "period_id")
    private UUID periodId;

    @Column(name = "invoice_ref", length = 100)
    private String invoiceRef;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "paid_at")
    private LocalDate paidAt;

    @Column(name = "tx_id")
    private UUID txId;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ExpenseEntity() {}

    public ExpenseEntity(UUID id, UUID orgId, UUID vendorId, String payeeText,
                         Integer categoryId, UUID costCentreId, UUID missionNodeId,
                         UUID periodId, String invoiceRef, BigDecimal amount, String currency,
                         String description, UUID createdBy) {
        this.id = id; this.orgId = orgId; this.vendorId = vendorId;
        this.payeeText = payeeText; this.categoryId = categoryId;
        this.costCentreId = costCentreId; this.missionNodeId = missionNodeId;
        this.periodId = periodId; this.invoiceRef = invoiceRef;
        this.amount = amount; this.currency = currency;
        this.description = description; this.status = "DRAFT";
        this.createdBy = createdBy; this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getOrgId() { return orgId; }
    public UUID getVendorId() { return vendorId; }
    public String getPayeeText() { return payeeText; }
    public Integer getCategoryId() { return categoryId; }
    public UUID getCostCentreId() { return costCentreId; }
    public UUID getMissionNodeId() { return missionNodeId; }
    public UUID getPeriodId() { return periodId; }
    public String getInvoiceRef() { return invoiceRef; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDate paidAt) { this.paidAt = paidAt; }
    public UUID getTxId() { return txId; }
    public void setTxId(UUID txId) { this.txId = txId; }
    public UUID getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }
}

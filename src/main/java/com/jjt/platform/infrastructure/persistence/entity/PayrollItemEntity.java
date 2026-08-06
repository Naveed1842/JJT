package com.jjt.platform.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "payroll_items")
public class PayrollItemEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "run_id", nullable = false)
    private UUID runId;

    @Column(name = "person_id", nullable = false)
    private UUID personId;

    @Column(name = "gross_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal grossAmount;

    @Column(name = "deductions", nullable = false, precision = 15, scale = 2)
    private BigDecimal deductions;

    @Column(name = "net_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal netAmount;

    @Column(name = "tx_id")
    private UUID txId;

    protected PayrollItemEntity() {}

    public PayrollItemEntity(UUID id, UUID runId, UUID personId, BigDecimal grossAmount,
                             BigDecimal deductions, BigDecimal netAmount) {
        this.id = id; this.runId = runId; this.personId = personId;
        this.grossAmount = grossAmount; this.deductions = deductions;
        this.netAmount = netAmount;
    }

    public UUID getId() { return id; }
    public UUID getRunId() { return runId; }
    public UUID getPersonId() { return personId; }
    public BigDecimal getGrossAmount() { return grossAmount; }
    public BigDecimal getDeductions() { return deductions; }
    public BigDecimal getNetAmount() { return netAmount; }
    public UUID getTxId() { return txId; }
    public void setTxId(UUID txId) { this.txId = txId; }
}

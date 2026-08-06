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
@Table(name = "approval_check_results")
public class ApprovalCheckResultEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "request_id", nullable = false)
    private UUID requestId;

    @Column(name = "check_type", nullable = false, length = 40)
    private String checkType;

    @Column(name = "verdict", nullable = false, length = 10)
    private String verdict;

    @Column(name = "confidence", nullable = false, precision = 4, scale = 3)
    private BigDecimal confidence;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "explanation", columnDefinition = "jsonb")
    private String explanation;

    @Column(name = "ran_at", nullable = false, updatable = false)
    private Instant ranAt;

    protected ApprovalCheckResultEntity() {}

    public ApprovalCheckResultEntity(UUID id, UUID requestId, String checkType,
                                     String verdict, BigDecimal confidence, String explanation) {
        this.id = id; this.requestId = requestId; this.checkType = checkType;
        this.verdict = verdict; this.confidence = confidence;
        this.explanation = explanation; this.ranAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getRequestId() { return requestId; }
    public String getCheckType() { return checkType; }
    public String getVerdict() { return verdict; }
    public BigDecimal getConfidence() { return confidence; }
    public String getExplanation() { return explanation; }
    public Instant getRanAt() { return ranAt; }
}

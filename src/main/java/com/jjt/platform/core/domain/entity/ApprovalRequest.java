package com.jjt.platform.core.domain.entity;

import java.time.Instant;
import java.util.UUID;

public final class ApprovalRequest {
    private final UUID id;
    private final UUID orgId;
    private final ApprovalEntityType entityType;
    private final UUID entityId;
    private final ApprovalStatus status;
    private final UUID requestedBy;
    private final UUID approvedBy;
    private final Instant reviewedAt;
    private final String notes;
    private final Instant createdAt;

    public ApprovalRequest(UUID id, UUID orgId, ApprovalEntityType entityType, UUID entityId,
                           ApprovalStatus status, UUID requestedBy, UUID approvedBy,
                           Instant reviewedAt, String notes, Instant createdAt) {
        this.id = id; this.orgId = orgId; this.entityType = entityType;
        this.entityId = entityId; this.status = status; this.requestedBy = requestedBy;
        this.approvedBy = approvedBy; this.reviewedAt = reviewedAt;
        this.notes = notes; this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getOrgId() { return orgId; }
    public ApprovalEntityType getEntityType() { return entityType; }
    public UUID getEntityId() { return entityId; }
    public ApprovalStatus getStatus() { return status; }
    public UUID getRequestedBy() { return requestedBy; }
    public UUID getApprovedBy() { return approvedBy; }
    public Instant getReviewedAt() { return reviewedAt; }
    public String getNotes() { return notes; }
    public Instant getCreatedAt() { return createdAt; }

    public ApprovalRequest withStatus(ApprovalStatus newStatus) {
        return new ApprovalRequest(id, orgId, entityType, entityId, newStatus,
                requestedBy, approvedBy, reviewedAt, notes, createdAt);
    }

    public ApprovalRequest resolved(UUID resolvedBy, ApprovalStatus finalStatus, String resolveNotes) {
        return new ApprovalRequest(id, orgId, entityType, entityId, finalStatus,
                requestedBy, resolvedBy, Instant.now(), resolveNotes, createdAt);
    }
}

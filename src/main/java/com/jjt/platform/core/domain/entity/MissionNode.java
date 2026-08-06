package com.jjt.platform.core.domain.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public final class MissionNode {
    private final UUID id;
    private final UUID orgId;
    private final UUID parentId;
    private final MissionNodeKind kind;
    private final String name;
    private final String description;
    private final MissionNodeStatus status;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final BigDecimal targetAmount;
    private final Instant createdAt;

    public MissionNode(UUID id, UUID orgId, UUID parentId, MissionNodeKind kind,
                       String name, String description, MissionNodeStatus status,
                       LocalDate startDate, LocalDate endDate, BigDecimal targetAmount,
                       Instant createdAt) {
        this.id = id; this.orgId = orgId; this.parentId = parentId;
        this.kind = kind; this.name = name; this.description = description;
        this.status = status; this.startDate = startDate; this.endDate = endDate;
        this.targetAmount = targetAmount; this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getOrgId() { return orgId; }
    public UUID getParentId() { return parentId; }
    public MissionNodeKind getKind() { return kind; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public MissionNodeStatus getStatus() { return status; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public BigDecimal getTargetAmount() { return targetAmount; }
    public Instant getCreatedAt() { return createdAt; }
}

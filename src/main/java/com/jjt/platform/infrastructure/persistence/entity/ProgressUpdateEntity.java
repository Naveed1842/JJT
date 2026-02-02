package com.jjt.platform.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.UUID;

@Entity
@Table(name = "progress_updates",
       uniqueConstraints = @UniqueConstraint(name = "uk_child_month", columnNames = {"child_id", "update_month"}))
public class ProgressUpdateEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "child_id", nullable = false)
    private UUID childId;

    @Column(name = "update_month", nullable = false, length = 7)
    private String updateMonth; // YYYY-MM

    @Column(name = "summary", nullable = false, length = 2000)
    private String summary;

    protected ProgressUpdateEntity() {
    }

    public ProgressUpdateEntity(UUID id, UUID childId, String updateMonth, String summary) {
        this.id = id;
        this.childId = childId;
        this.updateMonth = updateMonth;
        this.summary = summary;
    }

    public UUID getId() {
        return id;
    }

    public UUID getChildId() {
        return childId;
    }

    public String getUpdateMonth() {
        return updateMonth;
    }

    public String getSummary() {
        return summary;
    }
}

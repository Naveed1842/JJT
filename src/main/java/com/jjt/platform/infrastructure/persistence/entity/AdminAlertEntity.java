package com.jjt.platform.infrastructure.persistence.entity;

import com.jjt.platform.core.domain.entity.AlertSeverity;
import com.jjt.platform.core.domain.entity.AlertType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "admin_alerts")
public class AdminAlertEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "organisation_id", nullable = false, updatable = false)
    private UUID organisationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false, length = 30, updatable = false)
    private AlertType alertType;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 10)
    private AlertSeverity severity;

    @Column(name = "title", nullable = false, updatable = false)
    private String title;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT", updatable = false)
    private String message;

    @Column(name = "related_entity_id", updatable = false)
    private UUID relatedEntityId;

    @Column(name = "related_entity_type", length = 50, updatable = false)
    private String relatedEntityType;

    @Column(name = "dismissed_at")
    private Instant dismissedAt;

    @Column(name = "dismissed_by")
    private UUID dismissedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected AdminAlertEntity() {}

    public AdminAlertEntity(UUID id, UUID organisationId, AlertType alertType, AlertSeverity severity,
                            String title, String message, UUID relatedEntityId,
                            String relatedEntityType, Instant createdAt) {
        this.id = id;
        this.organisationId = organisationId;
        this.alertType = alertType;
        this.severity = severity;
        this.title = title;
        this.message = message;
        this.relatedEntityId = relatedEntityId;
        this.relatedEntityType = relatedEntityType;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getOrganisationId() { return organisationId; }
    public AlertType getAlertType() { return alertType; }
    public AlertSeverity getSeverity() { return severity; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public UUID getRelatedEntityId() { return relatedEntityId; }
    public String getRelatedEntityType() { return relatedEntityType; }
    public Instant getDismissedAt() { return dismissedAt; }
    public UUID getDismissedBy() { return dismissedBy; }
    public Instant getCreatedAt() { return createdAt; }

    public void dismiss(UUID byUser) {
        this.dismissedAt = Instant.now();
        this.dismissedBy = byUser;
    }
}

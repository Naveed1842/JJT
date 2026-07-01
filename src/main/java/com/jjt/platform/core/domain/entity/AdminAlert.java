package com.jjt.platform.core.domain.entity;

import java.time.Instant;
import java.util.UUID;

public final class AdminAlert {

    private final UUID id;
    private final UUID organisationId;
    private final AlertType alertType;
    private final AlertSeverity severity;
    private final String title;
    private final String message;
    private final UUID relatedEntityId;
    private final String relatedEntityType;
    private final Instant dismissedAt;
    private final UUID dismissedBy;
    private final Instant createdAt;

    public AdminAlert(UUID id, UUID organisationId, AlertType alertType, AlertSeverity severity,
                      String title, String message, UUID relatedEntityId, String relatedEntityType,
                      Instant dismissedAt, UUID dismissedBy, Instant createdAt) {
        this.id = id;
        this.organisationId = organisationId;
        this.alertType = alertType;
        this.severity = severity;
        this.title = title;
        this.message = message;
        this.relatedEntityId = relatedEntityId;
        this.relatedEntityType = relatedEntityType;
        this.dismissedAt = dismissedAt;
        this.dismissedBy = dismissedBy;
        this.createdAt = createdAt;
    }

    public static AdminAlert createNew(UUID organisationId, AlertType alertType, AlertSeverity severity,
                                       String title, String message,
                                       UUID relatedEntityId, String relatedEntityType) {
        return new AdminAlert(UUID.randomUUID(), organisationId, alertType, severity, title, message,
                relatedEntityId, relatedEntityType, null, null, Instant.now());
    }

    public AdminAlert dismiss(UUID dismissedBy) {
        return new AdminAlert(id, organisationId, alertType, severity, title, message,
                relatedEntityId, relatedEntityType, Instant.now(), dismissedBy, createdAt);
    }

    public boolean isDismissed() { return dismissedAt != null; }

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
}

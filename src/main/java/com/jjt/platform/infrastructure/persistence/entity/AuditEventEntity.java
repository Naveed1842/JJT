package com.jjt.platform.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_events")
public class AuditEventEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "organisation_id", nullable = false, updatable = false)
    private UUID organisationId;

    @Column(name = "event_type", nullable = false, length = 60, updatable = false)
    private String eventType;

    @Column(name = "actor_id", updatable = false)
    private UUID actorId;

    @Column(name = "actor_email", length = 255, updatable = false)
    private String actorEmail;

    @Column(name = "entity_type", length = 50, updatable = false)
    private String entityType;

    @Column(name = "entity_id", updatable = false)
    private UUID entityId;

    @Column(name = "description", nullable = false, updatable = false)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected AuditEventEntity() {}

    public AuditEventEntity(UUID id, UUID organisationId, String eventType,
                            UUID actorId, String actorEmail,
                            String entityType, UUID entityId,
                            String description, Instant createdAt) {
        this.id = id;
        this.organisationId = organisationId;
        this.eventType = eventType;
        this.actorId = actorId;
        this.actorEmail = actorEmail;
        this.entityType = entityType;
        this.entityId = entityId;
        this.description = description;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getOrganisationId() { return organisationId; }
    public String getEventType() { return eventType; }
    public UUID getActorId() { return actorId; }
    public String getActorEmail() { return actorEmail; }
    public String getEntityType() { return entityType; }
    public UUID getEntityId() { return entityId; }
    public String getDescription() { return description; }
    public Instant getCreatedAt() { return createdAt; }
}

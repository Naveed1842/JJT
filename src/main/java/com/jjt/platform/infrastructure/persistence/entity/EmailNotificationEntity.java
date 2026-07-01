package com.jjt.platform.infrastructure.persistence.entity;

import com.jjt.platform.core.domain.entity.NotificationTemplate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "email_notifications")
public class EmailNotificationEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "organisation_id", nullable = false, updatable = false)
    private UUID organisationId;

    @Column(name = "recipient_email", nullable = false, updatable = false)
    private String recipientEmail;

    @Column(name = "recipient_name", updatable = false)
    private String recipientName;

    @Enumerated(EnumType.STRING)
    @Column(name = "template", nullable = false, length = 50, updatable = false)
    private NotificationTemplate template;

    @Column(name = "subject", nullable = false, updatable = false)
    private String subject;

    @Column(name = "status", nullable = false, length = 10)
    private String status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "related_entity_id", updatable = false)
    private UUID relatedEntityId;

    @Column(name = "related_entity_type", length = 50, updatable = false)
    private String relatedEntityType;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected EmailNotificationEntity() {}

    public EmailNotificationEntity(UUID id, UUID organisationId, String recipientEmail,
                                   String recipientName, NotificationTemplate template,
                                   String subject, String status, UUID relatedEntityId,
                                   String relatedEntityType) {
        this.id = id;
        this.organisationId = organisationId;
        this.recipientEmail = recipientEmail;
        this.recipientName = recipientName;
        this.template = template;
        this.subject = subject;
        this.status = status;
        this.relatedEntityId = relatedEntityId;
        this.relatedEntityType = relatedEntityType;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getOrganisationId() { return organisationId; }
    public String getRecipientEmail() { return recipientEmail; }
    public String getRecipientName() { return recipientName; }
    public NotificationTemplate getTemplate() { return template; }
    public String getSubject() { return subject; }
    public String getStatus() { return status; }
    public String getErrorMessage() { return errorMessage; }
    public UUID getRelatedEntityId() { return relatedEntityId; }
    public String getRelatedEntityType() { return relatedEntityType; }
    public Instant getSentAt() { return sentAt; }
    public Instant getCreatedAt() { return createdAt; }

    public void markSent() { this.status = "SENT"; this.sentAt = Instant.now(); }
    public void markFailed(String error) { this.status = "FAILED"; this.errorMessage = error; }
}

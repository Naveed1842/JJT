package com.jjt.platform.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "media_attachments")
public class MediaAttachmentEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "media_id", nullable = false)
    private UUID mediaId;

    @Column(name = "owner_type", nullable = false, length = 50)
    private String ownerType;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "attachment_role", nullable = false, length = 50)
    private String attachmentRole;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "attached_by", nullable = false)
    private UUID attachedBy;

    @Column(name = "attached_at", nullable = false)
    private Instant attachedAt;

    protected MediaAttachmentEntity() {}

    public MediaAttachmentEntity(UUID id, UUID mediaId, String ownerType, UUID ownerId,
                                 String attachmentRole, int sortOrder, UUID attachedBy) {
        this.id = id;
        this.mediaId = mediaId;
        this.ownerType = ownerType;
        this.ownerId = ownerId;
        this.attachmentRole = attachmentRole;
        this.sortOrder = sortOrder;
        this.attachedBy = attachedBy;
        this.attachedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getMediaId() { return mediaId; }
    public String getOwnerType() { return ownerType; }
    public UUID getOwnerId() { return ownerId; }
    public String getAttachmentRole() { return attachmentRole; }
    public int getSortOrder() { return sortOrder; }
    public UUID getAttachedBy() { return attachedBy; }
    public Instant getAttachedAt() { return attachedAt; }

    public void setAttachmentRole(String attachmentRole) { this.attachmentRole = attachmentRole; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}

package com.jjt.platform.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "media_files")
public class MediaFileEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "org_id", nullable = false)
    private UUID orgId;

    @Column(name = "storage_ref", nullable = false, length = 500)
    private String storageRef;

    @Column(name = "storage_provider", nullable = false, length = 20)
    private String storageProvider;

    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "media_type", nullable = false, length = 20)
    private String mediaType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(name = "width_px")
    private Integer widthPx;

    @Column(name = "height_px")
    private Integer heightPx;

    @Column(name = "duration_secs")
    private Integer durationSecs;

    @Column(name = "visibility", nullable = false, length = 20)
    private String visibility;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "content_hash", length = 64)
    private String contentHash;

    @Column(name = "alt_text", length = 500)
    private String altText;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata = "{}";

    @Column(name = "uploaded_by", nullable = false)
    private UUID uploadedBy;

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected MediaFileEntity() {}

    public MediaFileEntity(UUID id, UUID orgId, String storageRef, String storageProvider,
                           String originalName, String mimeType, String mediaType,
                           String visibility, String status, UUID uploadedBy) {
        this.id = id;
        this.orgId = orgId;
        this.storageRef = storageRef;
        this.storageProvider = storageProvider;
        this.originalName = originalName;
        this.mimeType = mimeType;
        this.mediaType = mediaType;
        this.visibility = visibility;
        this.status = status;
        this.uploadedBy = uploadedBy;
        this.uploadedAt = Instant.now();
        this.sizeBytes = 0L;
    }

    public UUID getId() { return id; }
    public UUID getOrgId() { return orgId; }
    public String getStorageRef() { return storageRef; }
    public String getStorageProvider() { return storageProvider; }
    public String getOriginalName() { return originalName; }
    public String getMimeType() { return mimeType; }
    public String getMediaType() { return mediaType; }
    public long getSizeBytes() { return sizeBytes; }
    public Integer getWidthPx() { return widthPx; }
    public Integer getHeightPx() { return heightPx; }
    public Integer getDurationSecs() { return durationSecs; }
    public String getVisibility() { return visibility; }
    public String getStatus() { return status; }
    public String getContentHash() { return contentHash; }
    public String getAltText() { return altText; }
    public String getMetadata() { return metadata; }
    public UUID getUploadedBy() { return uploadedBy; }
    public Instant getUploadedAt() { return uploadedAt; }
    public Instant getDeletedAt() { return deletedAt; }

    public void setStatus(String status) { this.status = status; }
    public void setSizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; }
    public void setContentHash(String contentHash) { this.contentHash = contentHash; }
    public void setWidthPx(Integer widthPx) { this.widthPx = widthPx; }
    public void setHeightPx(Integer heightPx) { this.heightPx = heightPx; }
    public void setAltText(String altText) { this.altText = altText; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }
}

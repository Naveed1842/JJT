package com.jjt.platform.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "media_variants")
public class MediaVariantEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "media_id", nullable = false)
    private UUID mediaId;

    @Column(name = "variant_type", nullable = false, length = 30)
    private String variantType;

    @Column(name = "storage_ref", nullable = false, length = 500)
    private String storageRef;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(name = "width_px")
    private Integer widthPx;

    @Column(name = "height_px")
    private Integer heightPx;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected MediaVariantEntity() {}

    public MediaVariantEntity(UUID id, UUID mediaId, String variantType,
                              String storageRef, String mimeType, long sizeBytes,
                              Integer widthPx, Integer heightPx) {
        this.id = id;
        this.mediaId = mediaId;
        this.variantType = variantType;
        this.storageRef = storageRef;
        this.mimeType = mimeType;
        this.sizeBytes = sizeBytes;
        this.widthPx = widthPx;
        this.heightPx = heightPx;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getMediaId() { return mediaId; }
    public String getVariantType() { return variantType; }
    public String getStorageRef() { return storageRef; }
    public String getMimeType() { return mimeType; }
    public long getSizeBytes() { return sizeBytes; }
    public Integer getWidthPx() { return widthPx; }
    public Integer getHeightPx() { return heightPx; }
    public Instant getCreatedAt() { return createdAt; }
}

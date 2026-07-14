package com.jjt.platform.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "media_import_items")
public class MediaImportItemEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "job_id", nullable = false)
    private UUID jobId;

    @Column(name = "source_filename", nullable = false, length = 255)
    private String sourceFilename;

    @Column(name = "matched_entity_type", length = 50)
    private String matchedEntityType;

    @Column(name = "matched_entity_id")
    private UUID matchedEntityId;

    @Column(name = "match_key", length = 100)
    private String matchKey;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "media_id")
    private UUID mediaId;

    @Column(name = "error_detail")
    private String errorDetail;

    protected MediaImportItemEntity() {}

    public MediaImportItemEntity(UUID id, UUID jobId, String sourceFilename, String status) {
        this.id = id;
        this.jobId = jobId;
        this.sourceFilename = sourceFilename;
        this.status = status;
    }

    public UUID getId() { return id; }
    public UUID getJobId() { return jobId; }
    public String getSourceFilename() { return sourceFilename; }
    public String getMatchedEntityType() { return matchedEntityType; }
    public UUID getMatchedEntityId() { return matchedEntityId; }
    public String getMatchKey() { return matchKey; }
    public String getStatus() { return status; }
    public UUID getMediaId() { return mediaId; }
    public String getErrorDetail() { return errorDetail; }

    public void setMatchedEntityType(String matchedEntityType) { this.matchedEntityType = matchedEntityType; }
    public void setMatchedEntityId(UUID matchedEntityId) { this.matchedEntityId = matchedEntityId; }
    public void setMatchKey(String matchKey) { this.matchKey = matchKey; }
    public void setStatus(String status) { this.status = status; }
    public void setMediaId(UUID mediaId) { this.mediaId = mediaId; }
    public void setErrorDetail(String errorDetail) { this.errorDetail = errorDetail; }
}

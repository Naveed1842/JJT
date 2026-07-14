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
@Table(name = "media_import_jobs")
public class MediaImportJobEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "org_id", nullable = false)
    private UUID orgId;

    @Column(name = "job_type", nullable = false, length = 30)
    private String jobType;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "archive_ref", length = 500)
    private String archiveRef;

    @Column(name = "total_files", nullable = false)
    private int totalFiles;

    @Column(name = "matched", nullable = false)
    private int matched;

    @Column(name = "uploaded", nullable = false)
    private int uploaded;

    @Column(name = "skipped", nullable = false)
    private int skipped;

    @Column(name = "failed", nullable = false)
    private int failed;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "error_report", columnDefinition = "jsonb", nullable = false)
    private String errorReport = "[]";

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected MediaImportJobEntity() {}

    public MediaImportJobEntity(UUID id, UUID orgId, String jobType, UUID createdBy) {
        this.id = id;
        this.orgId = orgId;
        this.jobType = jobType;
        this.status = "QUEUED";
        this.createdBy = createdBy;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getOrgId() { return orgId; }
    public String getJobType() { return jobType; }
    public String getStatus() { return status; }
    public String getArchiveRef() { return archiveRef; }
    public int getTotalFiles() { return totalFiles; }
    public int getMatched() { return matched; }
    public int getUploaded() { return uploaded; }
    public int getSkipped() { return skipped; }
    public int getFailed() { return failed; }
    public String getErrorReport() { return errorReport; }
    public UUID getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getCompletedAt() { return completedAt; }

    public void setStatus(String status) { this.status = status; }
    public void setArchiveRef(String archiveRef) { this.archiveRef = archiveRef; }
    public void setTotalFiles(int totalFiles) { this.totalFiles = totalFiles; }
    public void setMatched(int matched) { this.matched = matched; }
    public void setUploaded(int uploaded) { this.uploaded = uploaded; }
    public void setSkipped(int skipped) { this.skipped = skipped; }
    public void setFailed(int failed) { this.failed = failed; }
    public void setErrorReport(String errorReport) { this.errorReport = errorReport; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}

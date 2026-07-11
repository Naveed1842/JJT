package com.jjt.platform.api.media.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jjt.platform.api.media.dto.BulkImportJobResponse;
import com.jjt.platform.infrastructure.media.StorageProvider;
import com.jjt.platform.infrastructure.persistence.entity.MediaFileEntity;
import com.jjt.platform.infrastructure.persistence.entity.MediaImportItemEntity;
import com.jjt.platform.infrastructure.persistence.entity.MediaImportJobEntity;
import com.jjt.platform.infrastructure.persistence.repository.ChildJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.MediaFileRepository;
import com.jjt.platform.infrastructure.persistence.repository.MediaImportItemRepository;
import com.jjt.platform.infrastructure.persistence.repository.MediaImportJobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class BulkImportService {

    private static final Logger log = LoggerFactory.getLogger(BulkImportService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final StorageProvider storage;
    private final MediaFileRepository mediaFileRepo;
    private final MediaImportJobRepository jobRepo;
    private final MediaImportItemRepository itemRepo;
    private final ChildJpaRepository childRepo;
    private final MediaService mediaService;
    private final MediaProcessingService processingService;

    public BulkImportService(StorageProvider storage,
                             MediaFileRepository mediaFileRepo,
                             MediaImportJobRepository jobRepo,
                             MediaImportItemRepository itemRepo,
                             ChildJpaRepository childRepo,
                             MediaService mediaService,
                             MediaProcessingService processingService) {
        this.storage = storage;
        this.mediaFileRepo = mediaFileRepo;
        this.jobRepo = jobRepo;
        this.itemRepo = itemRepo;
        this.childRepo = childRepo;
        this.mediaService = mediaService;
        this.processingService = processingService;
    }

    /** Persist the job record synchronously, then hand off processing to the async thread pool. */
    @Transactional
    public UUID createJob(MultipartFile archive, UUID orgId, UUID userId) throws IOException {
        UUID jobId = UUID.randomUUID();
        MediaImportJobEntity job = new MediaImportJobEntity(jobId, orgId, "BULK_CHILD_PHOTOS", userId);
        jobRepo.save(job);

        byte[] zipBytes = archive.getBytes();
        processAsync(jobId, zipBytes, orgId, userId);
        return jobId;
    }

    @Transactional(readOnly = true)
    public BulkImportJobResponse getJob(UUID jobId, UUID orgId) {
        MediaImportJobEntity job = jobRepo.findByIdAndOrgId(jobId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Import job not found: " + jobId));
        List<MediaImportItemEntity> items = itemRepo.findByJobIdAndStatus(jobId, "FAILED");
        return toResponse(job, items);
    }

    // ── Async processing ──────────────────────────────────────────────────────

    @Async("mediaProcessingExecutor")
    public void processAsync(UUID jobId, byte[] zipBytes, UUID orgId, UUID userId) {
        MediaImportJobEntity job = jobRepo.findById(jobId).orElseThrow();
        job.setStatus("PROCESSING");
        jobRepo.save(job);

        int total = 0, matched = 0, uploaded = 0, skipped = 0, failed = 0;
        List<BulkImportJobResponse.ImportErrorEntry> errors = new ArrayList<>();

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;
                String name = leafName(entry.getName());
                if (!isImage(name)) continue;

                total++;
                byte[] bytes = zis.readAllBytes();

                try {
                    String mimeType = mimeTypeFor(name);
                    String ext = ext(name);
                    String rollNumber = stem(name);

                    var childOpt = childRepo.findByRollNumberAndOrganisationId(rollNumber, orgId);
                    if (childOpt.isEmpty()) {
                        errors.add(new BulkImportJobResponse.ImportErrorEntry(name, "No child with roll number: " + rollNumber));
                        failed++;
                        recordItem(jobId, name, null, rollNumber, "FAILED", null, "No child with roll number: " + rollNumber);
                        continue;
                    }
                    var child = childOpt.get();
                    matched++;

                    // SHA-256 dedup
                    String hash = sha256Hex(bytes);
                    var existing = mediaFileRepo.findByOrgIdAndContentHash(orgId, hash);
                    if (!existing.isEmpty()) {
                        // Re-attach the existing file as profile photo instead of uploading a duplicate
                        MediaFileEntity existingMedia = existing.get(0);
                        mediaService.attachMedia(existingMedia, "CHILD", child.getId(), "PROFILE_PHOTO", 0, userId);
                        skipped++;
                        recordItem(jobId, name, "CHILD", rollNumber, "SKIPPED", existingMedia.getId(), null);
                        continue;
                    }

                    // Upload to storage
                    UUID mediaId = UUID.randomUUID();
                    String storageRef = orgId + "/child/" + child.getId() + "/" + mediaId + "/original." + ext;
                    storage.upload(new ByteArrayInputStream(bytes), storageRef, mimeType, bytes.length);

                    // Persist media record
                    MediaFileEntity mediaEntity = new MediaFileEntity(
                            mediaId, orgId, storageRef, storage.providerName(),
                            name, mimeType, "IMAGE", "PUBLIC", "PROCESSING", userId);
                    mediaEntity.setContentHash(hash);
                    mediaEntity.setSizeBytes(bytes.length);
                    mediaFileRepo.save(mediaEntity);

                    // Attach as profile photo (demotes existing)
                    mediaService.attachMedia(mediaEntity, "CHILD", child.getId(), "PROFILE_PHOTO", 0, userId);

                    // Trigger async thumbnail generation
                    processingService.process(mediaId);

                    uploaded++;
                    recordItem(jobId, name, "CHILD", rollNumber, "UPLOADED", mediaId, null);

                } catch (Exception e) {
                    log.error("Bulk import failed for entry {}: {}", name, e.getMessage(), e);
                    errors.add(new BulkImportJobResponse.ImportErrorEntry(name, e.getMessage()));
                    failed++;
                    recordItem(jobId, name, null, stem(name), "FAILED", null, e.getMessage());
                }

                zis.closeEntry();
            }
        } catch (IOException e) {
            log.error("Failed to read ZIP for jobId={}: {}", jobId, e.getMessage(), e);
            job.setStatus("FAILED");
            jobRepo.save(job);
            return;
        }

        job.setTotalFiles(total);
        job.setMatched(matched);
        job.setUploaded(uploaded);
        job.setSkipped(skipped);
        job.setFailed(failed);
        job.setStatus(failed > 0 ? "COMPLETED_WITH_ERRORS" : "COMPLETED");
        job.setCompletedAt(Instant.now());
        try {
            job.setErrorReport(MAPPER.writeValueAsString(errors));
        } catch (JsonProcessingException ignored) {}
        jobRepo.save(job);
    }

    private void recordItem(UUID jobId, String filename, String entityType, String matchKey,
                            String status, UUID mediaId, String errorDetail) {
        MediaImportItemEntity item = new MediaImportItemEntity(UUID.randomUUID(), jobId, filename, status);
        item.setMatchedEntityType(entityType);
        item.setMatchKey(matchKey);
        item.setMediaId(mediaId);
        item.setErrorDetail(errorDetail);
        itemRepo.save(item);
    }

    private static String sha256Hex(byte[] bytes) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private static String leafName(String path) {
        int slash = path.lastIndexOf('/');
        return slash >= 0 ? path.substring(slash + 1) : path;
    }

    private static String stem(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(0, dot) : filename;
    }

    private static String ext(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(dot + 1).toLowerCase() : "bin";
    }

    private static boolean isImage(String filename) {
        String e = ext(filename);
        return e.equals("jpg") || e.equals("jpeg") || e.equals("png") || e.equals("webp");
    }

    private static String mimeTypeFor(String filename) {
        return switch (ext(filename)) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            default -> "image/jpeg";
        };
    }

    private static BulkImportJobResponse toResponse(MediaImportJobEntity job,
                                                     List<MediaImportItemEntity> failedItems) {
        List<BulkImportJobResponse.ImportErrorEntry> errors = failedItems.stream()
                .map(i -> new BulkImportJobResponse.ImportErrorEntry(
                        i.getSourceFilename(), i.getErrorDetail()))
                .toList();
        return new BulkImportJobResponse(
                job.getId(), job.getJobType(), job.getStatus(),
                job.getTotalFiles(), job.getMatched(), job.getUploaded(),
                job.getSkipped(), job.getFailed(),
                errors, job.getCreatedAt(), job.getCompletedAt());
    }
}

package com.jjt.platform.api.media.service;

import com.jjt.platform.api.media.dto.MediaAttachmentResponse;
import com.jjt.platform.api.media.dto.MediaConfirmRequest;
import com.jjt.platform.api.media.dto.MediaFileResponse;
import com.jjt.platform.api.media.dto.MediaUploadIntentRequest;
import com.jjt.platform.api.media.dto.MediaUploadIntentResponse;
import com.jjt.platform.api.media.dto.MediaVariantResponse;
import com.jjt.platform.infrastructure.media.PresignedPutUrl;
import com.jjt.platform.infrastructure.media.StorageProvider;
import com.jjt.platform.infrastructure.persistence.entity.ChildEntity;
import com.jjt.platform.infrastructure.persistence.entity.MediaAttachmentEntity;
import com.jjt.platform.infrastructure.persistence.entity.MediaFileEntity;
import com.jjt.platform.infrastructure.persistence.entity.MediaVariantEntity;
import com.jjt.platform.infrastructure.persistence.repository.ChildJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.MediaAttachmentRepository;
import com.jjt.platform.infrastructure.persistence.repository.MediaFileRepository;
import com.jjt.platform.infrastructure.persistence.repository.MediaVariantRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MediaService {

    private static final Duration PRESIGNED_PUT_EXPIRY = Duration.ofMinutes(15);
    private static final Duration PRESIGNED_GET_EXPIRY = Duration.ofHours(1);

    private final StorageProvider storage;
    private final MediaFileRepository mediaFileRepo;
    private final MediaVariantRepository variantRepo;
    private final MediaAttachmentRepository attachmentRepo;
    private final ChildJpaRepository childRepo;
    private final MediaProcessingService processingService;

    @Value("${media.storage.max-upload-size-bytes:52428800}")
    private long maxUploadSizeBytes;

    public MediaService(StorageProvider storage,
                        MediaFileRepository mediaFileRepo,
                        MediaVariantRepository variantRepo,
                        MediaAttachmentRepository attachmentRepo,
                        ChildJpaRepository childRepo,
                        MediaProcessingService processingService) {
        this.storage = storage;
        this.mediaFileRepo = mediaFileRepo;
        this.variantRepo = variantRepo;
        this.attachmentRepo = attachmentRepo;
        this.childRepo = childRepo;
        this.processingService = processingService;
    }

    // ── Upload intent ────────────────────────────────────────────────────────

    @Transactional
    public MediaUploadIntentResponse createUploadIntent(MediaUploadIntentRequest req, UUID orgId, UUID userId) {
        validateMimeType(req.mimeType());
        if (req.sizeBytes() > maxUploadSizeBytes) {
            throw new IllegalArgumentException("File exceeds maximum allowed size of " + maxUploadSizeBytes + " bytes");
        }

        UUID mediaId = UUID.randomUUID();
        String ext = extensionFor(req.originalName(), req.mimeType());
        String storageRef = orgId + "/" + req.ownerType().toLowerCase() + "/" + req.ownerId()
                + "/" + mediaId + "/original." + ext;

        String visibility = Optional.ofNullable(req.visibility()).orElse("PRIVATE");
        String mediaType = mediaTypeFor(req.mimeType());

        MediaFileEntity entity = new MediaFileEntity(
                mediaId, orgId, storageRef, storage.providerName(),
                req.originalName(), req.mimeType(), mediaType, visibility, "UPLOADING", userId);
        mediaFileRepo.save(entity);

        // Persist pending attachment info in the entity's metadata via status tracking
        // We'll create the attachment at confirm time using data in a separate pending record.
        // For simplicity, store pending attachment via a transient token map in service.
        pendingAttachments.put(mediaId, new PendingAttachment(
                req.ownerType(), req.ownerId(), req.attachmentRole(), req.sortOrder(), userId));

        PresignedPutUrl presigned = storage.generatePresignedPutUrl(
                storageRef, req.mimeType(), maxUploadSizeBytes, PRESIGNED_PUT_EXPIRY);

        return new MediaUploadIntentResponse(
                mediaId, presigned.uploadUrl(), storageRef, presigned.expiresAt(), maxUploadSizeBytes);
    }

    // ── Confirm upload ───────────────────────────────────────────────────────

    @Transactional
    public MediaFileResponse confirmUpload(UUID mediaId, MediaConfirmRequest req, UUID userId) {
        MediaFileEntity media = mediaFileRepo.findByIdAndDeletedAtIsNull(mediaId)
                .orElseThrow(() -> new IllegalArgumentException("Media not found: " + mediaId));

        if (!"UPLOADING".equals(media.getStatus())) {
            throw new IllegalStateException("Media is not in UPLOADING state: " + media.getStatus());
        }

        if (req.contentHash() != null) {
            media.setContentHash(req.contentHash());
        }
        media.setStatus("PROCESSING");
        mediaFileRepo.save(media);

        // Create attachment
        PendingAttachment pending = pendingAttachments.remove(mediaId);
        if (pending != null) {
            createAttachment(media, pending);
        }

        // Trigger async processing (thumbnail generation)
        processingService.process(mediaId);

        return toFileResponse(media, List.of());
    }

    // ── Attachment management ────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<MediaAttachmentResponse> listAttachments(String ownerType, UUID ownerId, String role) {
        List<MediaAttachmentEntity> attachments = role != null
                ? attachmentRepo.findByOwnerTypeAndOwnerIdAndAttachmentRoleOrderBySortOrder(ownerType, ownerId, role)
                : attachmentRepo.findByOwnerTypeAndOwnerIdOrderBySortOrder(ownerType, ownerId);

        return attachments.stream()
                .map(a -> {
                    MediaFileEntity mf = mediaFileRepo.findById(a.getMediaId()).orElse(null);
                    if (mf == null || mf.getDeletedAt() != null) return null;
                    List<MediaVariantEntity> variants = variantRepo.findByMediaId(mf.getId());
                    return toAttachmentResponse(a, mf, variants);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /** Batch lookup of profile photo URLs for a list of children. */
    @Transactional(readOnly = true)
    public Map<UUID, String> resolveProfilePhotoUrls(List<UUID> childIds) {
        if (childIds.isEmpty()) return Map.of();

        List<MediaAttachmentEntity> attachments = attachmentRepo
                .findByOwnerTypeAndOwnerIdsAndRole("CHILD", childIds, "PROFILE_PHOTO");

        if (attachments.isEmpty()) return Map.of();

        List<UUID> mediaIds = attachments.stream().map(MediaAttachmentEntity::getMediaId).toList();
        Map<UUID, MediaFileEntity> mediaMap = mediaFileRepo.findAllActiveByIds(mediaIds).stream()
                .collect(Collectors.toMap(MediaFileEntity::getId, m -> m));

        return attachments.stream()
                .filter(a -> mediaMap.containsKey(a.getMediaId()))
                .collect(Collectors.toMap(
                        MediaAttachmentEntity::getOwnerId,
                        a -> resolveUrl(mediaMap.get(a.getMediaId()), "THUMBNAIL_MD"),
                        (existing, replacement) -> existing // keep first (lowest sort_order)
                ));
    }

    @Transactional(readOnly = true)
    public MediaFileResponse getMedia(UUID mediaId) {
        MediaFileEntity media = mediaFileRepo.findByIdAndDeletedAtIsNull(mediaId)
                .orElseThrow(() -> new IllegalArgumentException("Media not found: " + mediaId));
        List<MediaVariantEntity> variants = variantRepo.findByMediaId(mediaId);
        return toFileResponse(media, variants);
    }

    @Transactional(readOnly = true)
    public String getMediaUrl(UUID mediaId) {
        MediaFileEntity media = mediaFileRepo.findByIdAndDeletedAtIsNull(mediaId)
                .orElseThrow(() -> new IllegalArgumentException("Media not found: " + mediaId));
        if ("PUBLIC".equals(media.getVisibility())) {
            return storage.getPublicUrl(media.getStorageRef());
        }
        return storage.generatePresignedGetUrl(media.getStorageRef(), PRESIGNED_GET_EXPIRY);
    }

    @Transactional
    public void softDelete(UUID mediaId, UUID userId) {
        MediaFileEntity media = mediaFileRepo.findByIdAndDeletedAtIsNull(mediaId)
                .orElseThrow(() -> new IllegalArgumentException("Media not found: " + mediaId));
        media.setDeletedAt(Instant.now());
        mediaFileRepo.save(media);
        // S3 deletion scheduled via storage provider (async in prod)
        storage.delete(media.getStorageRef());
    }

    // ── Called by processing service after variant generation ────────────────

    @Transactional
    public void markReady(UUID mediaId, long sizeBytes, Integer widthPx, Integer heightPx) {
        mediaFileRepo.findByIdAndDeletedAtIsNull(mediaId).ifPresent(m -> {
            m.setSizeBytes(sizeBytes);
            m.setWidthPx(widthPx);
            m.setHeightPx(heightPx);
            m.setStatus("READY");
            mediaFileRepo.save(m);
        });
    }

    @Transactional
    public void markFailed(UUID mediaId) {
        mediaFileRepo.findByIdAndDeletedAtIsNull(mediaId).ifPresent(m -> {
            m.setStatus("FAILED");
            mediaFileRepo.save(m);
        });
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void createAttachment(MediaFileEntity media, PendingAttachment pending) {
        // If PROFILE_PHOTO: demote existing to GALLERY
        if ("PROFILE_PHOTO".equalsIgnoreCase(pending.attachmentRole())) {
            attachmentRepo.findFirstByOwnerTypeAndOwnerIdAndAttachmentRole(
                    pending.ownerType(), pending.ownerId(), "PROFILE_PHOTO")
                    .ifPresent(existing -> {
                        existing.setAttachmentRole("GALLERY");
                        existing.setSortOrder(999);
                        attachmentRepo.save(existing);
                    });
        }

        MediaAttachmentEntity att = new MediaAttachmentEntity(
                UUID.randomUUID(), media.getId(),
                pending.ownerType(), pending.ownerId(),
                pending.attachmentRole(), pending.sortOrder(), pending.uploadedBy());
        attachmentRepo.save(att);

        // Update children.profile_photo_media_id denorm column
        if ("CHILD".equalsIgnoreCase(pending.ownerType())
                && "PROFILE_PHOTO".equalsIgnoreCase(pending.attachmentRole())) {
            childRepo.findById(pending.ownerId()).ifPresent(child -> {
                child.setProfilePhotoMediaId(media.getId());
                childRepo.save(child);
            });
        }
    }

    private String resolveUrl(MediaFileEntity media, String preferredVariant) {
        if (!"READY".equals(media.getStatus()) && !"PROCESSING".equals(media.getStatus())) {
            return storage.getPublicUrl(media.getStorageRef());
        }
        // Try preferred variant first
        return variantRepo.findByMediaIdAndVariantType(media.getId(), preferredVariant)
                .map(v -> storage.getPublicUrl(v.getStorageRef()))
                .orElseGet(() -> storage.getPublicUrl(media.getStorageRef()));
    }

    private MediaFileResponse toFileResponse(MediaFileEntity media, List<MediaVariantEntity> variants) {
        List<MediaVariantResponse> variantDtos = variants.stream()
                .map(v -> new MediaVariantResponse(
                        v.getVariantType(),
                        storage.getPublicUrl(v.getStorageRef()),
                        v.getMimeType(), v.getSizeBytes(), v.getWidthPx(), v.getHeightPx()))
                .toList();
        return new MediaFileResponse(
                media.getId(), media.getMediaType(), media.getMimeType(), media.getOriginalName(),
                media.getSizeBytes(), media.getWidthPx(), media.getHeightPx(),
                media.getVisibility(), media.getStatus(), media.getAltText(),
                media.getUploadedAt(), variantDtos);
    }

    private MediaAttachmentResponse toAttachmentResponse(MediaAttachmentEntity att,
                                                          MediaFileEntity mf,
                                                          List<MediaVariantEntity> variants) {
        String url = "PUBLIC".equals(mf.getVisibility())
                ? storage.getPublicUrl(mf.getStorageRef())
                : storage.generatePresignedGetUrl(mf.getStorageRef(), PRESIGNED_GET_EXPIRY);

        String thumbnailUrl = variants.stream()
                .filter(v -> "THUMBNAIL_MD".equals(v.getVariantType()))
                .findFirst()
                .map(v -> storage.getPublicUrl(v.getStorageRef()))
                .orElse(url);

        return new MediaAttachmentResponse(
                att.getId(), mf.getId(), att.getAttachmentRole(), att.getSortOrder(),
                url, thumbnailUrl, mf.getMimeType(), mf.getMediaType(),
                mf.getWidthPx(), mf.getHeightPx(), mf.getStatus());
    }

    private static String mediaTypeFor(String mimeType) {
        if (mimeType.startsWith("image/")) return "IMAGE";
        if (mimeType.startsWith("video/")) return "VIDEO";
        if (mimeType.startsWith("audio/")) return "AUDIO";
        if (mimeType.equals("application/pdf")) return "DOCUMENT";
        if (mimeType.contains("spreadsheet") || mimeType.contains("excel")) return "DOCUMENT";
        return "OTHER";
    }

    private static String extensionFor(String filename, String mimeType) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        }
        return switch (mimeType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            case "image/gif" -> "gif";
            case "application/pdf" -> "pdf";
            case "video/mp4" -> "mp4";
            case "audio/mpeg" -> "mp3";
            default -> "bin";
        };
    }

    private static final List<String> ALLOWED_MIME_TYPES = List.of(
            "image/jpeg", "image/png", "image/webp", "image/gif",
            "application/pdf", "video/mp4", "audio/mpeg",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-excel"
    );

    private static void validateMimeType(String mimeType) {
        if (!ALLOWED_MIME_TYPES.contains(mimeType)) {
            throw new IllegalArgumentException("File type not allowed: " + mimeType);
        }
    }

    /** Transient store for pending attachment info keyed by mediaId. */
    private final Map<UUID, PendingAttachment> pendingAttachments = new java.util.concurrent.ConcurrentHashMap<>();

    private record PendingAttachment(
            String ownerType, UUID ownerId, String attachmentRole, int sortOrder, UUID uploadedBy) {}
}

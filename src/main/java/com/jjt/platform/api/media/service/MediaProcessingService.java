package com.jjt.platform.api.media.service;

import com.jjt.platform.infrastructure.media.StorageProvider;
import com.jjt.platform.infrastructure.persistence.entity.MediaFileEntity;
import com.jjt.platform.infrastructure.persistence.entity.MediaVariantEntity;
import com.jjt.platform.infrastructure.persistence.repository.MediaFileRepository;
import com.jjt.platform.infrastructure.persistence.repository.MediaVariantRepository;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

@Service
public class MediaProcessingService {

    private static final Logger log = LoggerFactory.getLogger(MediaProcessingService.class);

    private static final int[] THUMBNAIL_SIZES = {100, 300, 600};
    private static final String[] THUMBNAIL_TYPES = {"THUMBNAIL_SM", "THUMBNAIL_MD", "THUMBNAIL_LG"};

    private final StorageProvider storage;
    private final MediaFileRepository mediaFileRepo;
    private final MediaVariantRepository variantRepo;
    private final MediaService mediaService;

    public MediaProcessingService(StorageProvider storage,
                                  MediaFileRepository mediaFileRepo,
                                  MediaVariantRepository variantRepo,
                                  @Lazy MediaService mediaService) {
        this.storage = storage;
        this.mediaFileRepo = mediaFileRepo;
        this.variantRepo = variantRepo;
        this.mediaService = mediaService;
    }

    @Async("mediaProcessingExecutor")
    public void process(UUID mediaId) {
        mediaFileRepo.findByIdAndDeletedAtIsNull(mediaId).ifPresent(media -> {
            try {
                if ("IMAGE".equals(media.getMediaType())) {
                    processImage(media);
                } else {
                    // Non-image: just mark ready (no variants generated in M1)
                    mediaService.markReady(mediaId, media.getSizeBytes(), null, null);
                }
            } catch (Exception e) {
                log.error("Media processing failed for {}: {}", mediaId, e.getMessage(), e);
                mediaService.markFailed(mediaId);
            }
        });
    }

    private void processImage(MediaFileEntity media) throws java.io.IOException {
        if (!storage.exists(media.getStorageRef())) {
            log.warn("Original not found in storage for mediaId={}, marking ready without variants", media.getId());
            mediaService.markReady(media.getId(), media.getSizeBytes(), null, null);
            return;
        }

        // Read original into memory for processing
        byte[] originalBytes = readFromStorage(media.getStorageRef());
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(originalBytes));

        if (originalImage == null) {
            log.warn("Could not decode image for mediaId={}", media.getId());
            mediaService.markReady(media.getId(), originalBytes.length, null, null);
            return;
        }

        int origWidth = originalImage.getWidth();
        int origHeight = originalImage.getHeight();

        // Generate square-crop thumbnails
        for (int i = 0; i < THUMBNAIL_SIZES.length; i++) {
            int size = THUMBNAIL_SIZES[i];
            String variantType = THUMBNAIL_TYPES[i];
            String variantRef = variantRefFor(media.getStorageRef(), variantType, "jpg");

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Thumbnails.of(new ByteArrayInputStream(originalBytes))
                    .size(size, size)
                    .crop(Positions.CENTER)
                    .outputFormat("jpg")
                    .toOutputStream(out);

            byte[] thumbBytes = out.toByteArray();
            storage.upload(new ByteArrayInputStream(thumbBytes), variantRef, "image/jpeg", thumbBytes.length);

            saveVariant(media.getId(), variantType, variantRef, "image/jpeg", thumbBytes.length, size, size);
            log.debug("Generated {} for mediaId={}", variantType, media.getId());
        }

        mediaService.markReady(media.getId(), originalBytes.length, origWidth, origHeight);
    }

    @Transactional
    public void saveVariant(UUID mediaId, String variantType, String storageRef,
                             String mimeType, long sizeBytes, Integer widthPx, Integer heightPx) {
        // Upsert: delete existing variant of same type if any
        variantRepo.findByMediaIdAndVariantType(mediaId, variantType)
                .ifPresent(variantRepo::delete);

        MediaVariantEntity variant = new MediaVariantEntity(
                UUID.randomUUID(), mediaId, variantType,
                storageRef, mimeType, sizeBytes, widthPx, heightPx);
        variantRepo.save(variant);
    }

    private byte[] readFromStorage(String storageRef) {
        return storage.download(storageRef);
    }

    private static String variantRefFor(String originalRef, String variantType, String ext) {
        // Replace "original.ext" with "{variantType}.ext"
        int lastSlash = originalRef.lastIndexOf('/');
        String dir = originalRef.substring(0, lastSlash + 1);
        return dir + variantType.toLowerCase() + "." + ext;
    }
}

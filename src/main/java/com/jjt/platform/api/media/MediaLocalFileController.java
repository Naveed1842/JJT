package com.jjt.platform.api.media;

import com.jjt.platform.infrastructure.media.local.LocalStorageProvider;
import com.jjt.platform.infrastructure.persistence.entity.MediaFileEntity;
import com.jjt.platform.infrastructure.persistence.repository.MediaFileRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Active only when the local storage provider is used (dev).
 * Serves uploaded files and accepts the simulated presigned PUT.
 */
@RestController
@RequestMapping
@ConditionalOnProperty(name = "media.storage.provider", havingValue = "local", matchIfMissing = true)
public class MediaLocalFileController {

    private static final Logger log = LoggerFactory.getLogger(MediaLocalFileController.class);

    private final LocalStorageProvider localProvider;
    private final MediaFileRepository mediaFileRepo;

    public MediaLocalFileController(LocalStorageProvider localProvider, MediaFileRepository mediaFileRepo) {
        this.localProvider = localProvider;
        this.mediaFileRepo = mediaFileRepo;
    }

    /** Serve a stored file by its storage ref path. */
    @GetMapping("/api/media/files/**")
    public ResponseEntity<Resource> serveFile(HttpServletRequest request) {
        String ref = request.getRequestURI().substring("/api/media/files/".length());
        Path filePath = localProvider.getUploadDir().resolve(ref).normalize();

        if (!filePath.startsWith(localProvider.getUploadDir())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        if (!Files.exists(filePath)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        String contentType;
        try {
            contentType = Files.probeContentType(filePath);
        } catch (IOException e) {
            contentType = "application/octet-stream";
        }
        if (contentType == null) contentType = "application/octet-stream";

        Resource resource = new FileSystemResource(filePath);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header("Cache-Control", "public, max-age=3600")
                .body(resource);
    }

    /**
     * Accept the simulated presigned PUT from the browser.
     * The URL token IS the mediaId — storageRef is looked up from the DB,
     * so no in-memory token store is required.
     */
    @PutMapping("/api/media/upload/{mediaId}")
    public ResponseEntity<Void> receiveUpload(
            @PathVariable UUID mediaId,
            HttpServletRequest request) {
        MediaFileEntity entity = mediaFileRepo.findByIdAndDeletedAtIsNull(mediaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.GONE, "No pending upload found"));

        if (!"UPLOADING".equals(entity.getStatus())) {
            throw new ResponseStatusException(HttpStatus.GONE, "Upload already completed or expired");
        }

        try {
            localProvider.storeUpload(entity.getStorageRef(), request.getInputStream());
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            log.error("Failed to store local upload for mediaId {}: {}", mediaId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

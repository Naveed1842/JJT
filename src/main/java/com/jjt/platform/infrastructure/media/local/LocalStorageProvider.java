package com.jjt.platform.infrastructure.media.local;

import com.jjt.platform.infrastructure.media.PresignedPutUrl;
import com.jjt.platform.infrastructure.media.StorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;

public class LocalStorageProvider implements StorageProvider {

    private static final Logger log = LoggerFactory.getLogger(LocalStorageProvider.class);

    private final Path uploadDir;
    private final String baseUrl;

    public LocalStorageProvider(
            @Value("${media.storage.upload-dir:${java.io.tmpdir}/jjt-media}") String uploadDir,
            @Value("${media.storage.local-base-url:http://localhost:8080}") String baseUrl) {
        this.uploadDir = Paths.get(uploadDir);
        this.baseUrl = baseUrl;
        try {
            Files.createDirectories(this.uploadDir);
            log.info("Local media storage initialised at {}", this.uploadDir.toAbsolutePath());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create local media upload dir: " + uploadDir, e);
        }
    }

    @Override
    public PresignedPutUrl generatePresignedPutUrl(String storageRef, String mimeType, long maxBytes, Duration expiry) {
        // storageRef = {orgId}/{ownerType}/{ownerId}/{mediaId}/original.{ext}
        // Use mediaId (index 3) in the URL — the controller looks up storageRef from the DB.
        String mediaId = storageRef.split("/")[3];
        String uploadUrl = baseUrl + "/api/media/upload/" + mediaId;
        return new PresignedPutUrl(uploadUrl, storageRef, Instant.now().plus(expiry));
    }

    /** Write uploaded bytes to the given storageRef path under uploadDir. */
    public void storeUpload(String storageRef, InputStream content) throws IOException {
        Path target = uploadDir.resolve(storageRef);
        Files.createDirectories(target.getParent());
        Files.copy(content, target, StandardCopyOption.REPLACE_EXISTING);
        log.debug("Local upload stored: {}", target);
    }

    @Override
    public String upload(InputStream content, String storageRef, String mimeType, long contentLength) {
        try {
            Path target = uploadDir.resolve(storageRef);
            Files.createDirectories(target.getParent());
            Files.copy(content, target, StandardCopyOption.REPLACE_EXISTING);
            return storageRef;
        } catch (IOException e) {
            throw new RuntimeException("Local upload failed for " + storageRef, e);
        }
    }

    @Override
    public byte[] download(String storageRef) {
        try {
            return Files.readAllBytes(uploadDir.resolve(storageRef));
        } catch (IOException e) {
            throw new RuntimeException("Local download failed for " + storageRef, e);
        }
    }

    @Override
    public String getPublicUrl(String storageRef) {
        return baseUrl + "/api/media/files/" + storageRef;
    }

    @Override
    public String generatePresignedGetUrl(String storageRef, Duration expiry) {
        return getPublicUrl(storageRef);
    }

    @Override
    public boolean exists(String storageRef) {
        return Files.exists(uploadDir.resolve(storageRef));
    }

    @Override
    public void delete(String storageRef) {
        try {
            Files.deleteIfExists(uploadDir.resolve(storageRef));
        } catch (IOException e) {
            log.warn("Could not delete local file {}: {}", storageRef, e.getMessage());
        }
    }

    @Override
    public String providerName() {
        return "LOCAL";
    }

    public Path getUploadDir() {
        return uploadDir;
    }
}

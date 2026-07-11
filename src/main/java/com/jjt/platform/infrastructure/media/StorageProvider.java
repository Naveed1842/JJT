package com.jjt.platform.infrastructure.media;

import java.io.InputStream;
import java.time.Duration;

public interface StorageProvider {

    /**
     * Generate a pre-signed PUT URL so the client can upload directly.
     * For the local provider this is a backend endpoint; for S3 it is a real S3 URL.
     */
    PresignedPutUrl generatePresignedPutUrl(String storageRef, String mimeType, long maxBytes, Duration expiry);

    /**
     * Upload a stream server-side (used by bulk import and variant generation).
     */
    String upload(InputStream content, String storageRef, String mimeType, long contentLength);

    /**
     * Download an object as a byte array (used by server-side image processing).
     */
    byte[] download(String storageRef);

    /**
     * Return a permanent public URL (for PUBLIC visibility assets served via CDN or local endpoint).
     */
    String getPublicUrl(String storageRef);

    /**
     * Return a time-limited signed GET URL for PRIVATE assets.
     */
    String generatePresignedGetUrl(String storageRef, Duration expiry);

    /** Check if an object exists. */
    boolean exists(String storageRef);

    /** Soft-delete: schedule or immediately remove the object. */
    void delete(String storageRef);

    /** Provider identifier stored in media_files.storage_provider. */
    String providerName();
}

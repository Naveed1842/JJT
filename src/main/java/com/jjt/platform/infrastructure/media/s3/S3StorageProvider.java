package com.jjt.platform.infrastructure.media.s3;

import com.jjt.platform.infrastructure.media.PresignedPutUrl;
import com.jjt.platform.infrastructure.media.StorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;

public class S3StorageProvider implements StorageProvider {

    private static final Logger log = LoggerFactory.getLogger(S3StorageProvider.class);

    private final S3Client s3;
    private final S3Presigner presigner;
    private final String bucket;
    private final String cdnBaseUrl;

    public S3StorageProvider(S3Client s3, S3Presigner presigner, String bucket, String cdnBaseUrl) {
        this.s3 = s3;
        this.presigner = presigner;
        this.bucket = bucket;
        this.cdnBaseUrl = cdnBaseUrl;
    }

    @Override
    public PresignedPutUrl generatePresignedPutUrl(String storageRef, String mimeType, long maxBytes, Duration expiry) {
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(expiry)
                .putObjectRequest(r -> r
                        .bucket(bucket)
                        .key(storageRef)
                        .contentType(mimeType)
                        .build())
                .build();
        PresignedPutObjectRequest presigned = presigner.presignPutObject(presignRequest);
        String uploadUrl = presigned.url().toString();
        Instant expiresAt = Instant.now().plus(expiry);
        log.debug("S3 presigned PUT URL generated for key: {}", storageRef);
        return new PresignedPutUrl(uploadUrl, storageRef, expiresAt);
    }

    @Override
    public String upload(InputStream content, String storageRef, String mimeType, long contentLength) {
        s3.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(storageRef)
                        .contentType(mimeType)
                        .contentLength(contentLength)
                        .build(),
                RequestBody.fromInputStream(content, contentLength)
        );
        return storageRef;
    }

    @Override
    public String getPublicUrl(String storageRef) {
        return cdnBaseUrl + "/" + storageRef;
    }

    @Override
    public String generatePresignedGetUrl(String storageRef, Duration expiry) {
        return presigner.presignGetObject(r -> r
                .signatureDuration(expiry)
                .getObjectRequest(g -> g.bucket(bucket).key(storageRef).build())
                .build()
        ).url().toString();
    }

    @Override
    public boolean exists(String storageRef) {
        try {
            s3.headObject(HeadObjectRequest.builder().bucket(bucket).key(storageRef).build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    @Override
    public void delete(String storageRef) {
        try {
            s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(storageRef).build());
        } catch (Exception e) {
            log.warn("S3 delete failed for {}: {}", storageRef, e.getMessage());
        }
    }

    @Override
    public String providerName() {
        return "S3";
    }
}

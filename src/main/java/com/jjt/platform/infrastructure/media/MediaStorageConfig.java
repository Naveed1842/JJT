package com.jjt.platform.infrastructure.media;

import com.jjt.platform.infrastructure.media.local.LocalStorageProvider;
import com.jjt.platform.infrastructure.media.s3.S3StorageProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class MediaStorageConfig {

    // ── Local (dev default) ──────────────────────────────────────────────────

    @Bean
    @ConditionalOnProperty(name = "media.storage.provider", havingValue = "local", matchIfMissing = true)
    public LocalStorageProvider localStorageProvider(
            @Value("${media.storage.upload-dir:${java.io.tmpdir}/jjt-media}") String uploadDir,
            @Value("${media.storage.local-base-url:http://localhost:8080}") String baseUrl) {
        return new LocalStorageProvider(uploadDir, baseUrl);
    }

    // ── AWS S3 (production) ─────────────────────────────────────────────────

    @Bean
    @ConditionalOnProperty(name = "media.storage.provider", havingValue = "s3")
    public S3Client s3Client(@Value("${media.storage.region:eu-west-2}") String region) {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "media.storage.provider", havingValue = "s3")
    public S3Presigner s3Presigner(@Value("${media.storage.region:eu-west-2}") String region) {
        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "media.storage.provider", havingValue = "s3")
    public StorageProvider s3StorageProvider(
            S3Client s3Client,
            S3Presigner s3Presigner,
            @Value("${media.storage.bucket}") String bucket,
            @Value("${media.storage.cdn-base-url}") String cdnBaseUrl) {
        return new S3StorageProvider(s3Client, s3Presigner, bucket, cdnBaseUrl);
    }
}

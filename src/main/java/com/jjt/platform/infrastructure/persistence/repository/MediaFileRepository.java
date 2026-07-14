package com.jjt.platform.infrastructure.persistence.repository;

import com.jjt.platform.infrastructure.persistence.entity.MediaFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MediaFileRepository extends JpaRepository<MediaFileEntity, UUID> {

    Optional<MediaFileEntity> findByIdAndDeletedAtIsNull(UUID id);

    @Query("SELECT m FROM MediaFileEntity m WHERE m.orgId = :orgId AND m.contentHash = :hash AND m.deletedAt IS NULL")
    List<MediaFileEntity> findByOrgIdAndContentHash(@Param("orgId") UUID orgId, @Param("hash") String hash);

    @Query("SELECT m FROM MediaFileEntity m WHERE m.id IN :ids AND m.deletedAt IS NULL")
    List<MediaFileEntity> findAllActiveByIds(@Param("ids") List<UUID> ids);
}

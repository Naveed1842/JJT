package com.jjt.platform.infrastructure.persistence.repository;

import com.jjt.platform.infrastructure.persistence.entity.MediaAttachmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MediaAttachmentRepository extends JpaRepository<MediaAttachmentEntity, UUID> {

    List<MediaAttachmentEntity> findByOwnerTypeAndOwnerIdOrderBySortOrder(String ownerType, UUID ownerId);

    List<MediaAttachmentEntity> findByOwnerTypeAndOwnerIdAndAttachmentRoleOrderBySortOrder(
            String ownerType, UUID ownerId, String attachmentRole);

    Optional<MediaAttachmentEntity> findFirstByOwnerTypeAndOwnerIdAndAttachmentRole(
            String ownerType, UUID ownerId, String attachmentRole);

    @Query("SELECT a FROM MediaAttachmentEntity a WHERE a.ownerType = :ownerType " +
           "AND a.ownerId IN :ownerIds AND a.attachmentRole = :role ORDER BY a.sortOrder")
    List<MediaAttachmentEntity> findByOwnerTypeAndOwnerIdsAndRole(
            @Param("ownerType") String ownerType,
            @Param("ownerIds") List<UUID> ownerIds,
            @Param("role") String role);

    boolean existsByMediaIdAndOwnerTypeAndOwnerIdAndAttachmentRole(
            UUID mediaId, String ownerType, UUID ownerId, String attachmentRole);
}

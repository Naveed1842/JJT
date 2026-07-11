package com.jjt.platform.infrastructure.persistence.repository;

import com.jjt.platform.infrastructure.persistence.entity.MediaImportJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MediaImportJobRepository extends JpaRepository<MediaImportJobEntity, UUID> {

    Optional<MediaImportJobEntity> findByIdAndOrgId(UUID id, UUID orgId);

    List<MediaImportJobEntity> findByOrgIdOrderByCreatedAtDesc(UUID orgId);
}

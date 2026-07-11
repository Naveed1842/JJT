package com.jjt.platform.infrastructure.persistence.repository;

import com.jjt.platform.infrastructure.persistence.entity.MediaImportItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MediaImportItemRepository extends JpaRepository<MediaImportItemEntity, UUID> {

    List<MediaImportItemEntity> findByJobId(UUID jobId);

    List<MediaImportItemEntity> findByJobIdAndStatus(UUID jobId, String status);
}

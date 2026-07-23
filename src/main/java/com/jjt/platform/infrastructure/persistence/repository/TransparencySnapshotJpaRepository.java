package com.jjt.platform.infrastructure.persistence.repository;

import com.jjt.platform.infrastructure.persistence.entity.TransparencySnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransparencySnapshotJpaRepository extends JpaRepository<TransparencySnapshotEntity, UUID> {
    Optional<TransparencySnapshotEntity> findByOrgIdAndPeriodId(UUID orgId, UUID periodId);
    Optional<TransparencySnapshotEntity> findFirstByOrgIdAndPublishedTrueOrderByComputedAtDesc(UUID orgId);
    List<TransparencySnapshotEntity> findByOrgIdAndPublishedTrueOrderByComputedAtDesc(UUID orgId);
}

package com.jjt.platform.infrastructure.persistence.repository;

import com.jjt.platform.infrastructure.persistence.entity.ApprovalRequestEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApprovalRequestJpaRepository extends JpaRepository<ApprovalRequestEntity, UUID> {
    Page<ApprovalRequestEntity> findByOrgIdOrderByCreatedAtDesc(UUID orgId, Pageable pageable);
    Page<ApprovalRequestEntity> findByOrgIdAndStatusOrderByCreatedAtDesc(UUID orgId, String status, Pageable pageable);
    Page<ApprovalRequestEntity> findByOrgIdAndEntityTypeOrderByCreatedAtDesc(UUID orgId, String entityType, Pageable pageable);
    Optional<ApprovalRequestEntity> findByEntityTypeAndEntityId(String entityType, UUID entityId);
    List<ApprovalRequestEntity> findByOrgIdAndStatusIn(UUID orgId, List<String> statuses);
}

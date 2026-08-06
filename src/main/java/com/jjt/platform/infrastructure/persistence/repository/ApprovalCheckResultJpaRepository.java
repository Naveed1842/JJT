package com.jjt.platform.infrastructure.persistence.repository;

import com.jjt.platform.infrastructure.persistence.entity.ApprovalCheckResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApprovalCheckResultJpaRepository extends JpaRepository<ApprovalCheckResultEntity, UUID> {
    List<ApprovalCheckResultEntity> findByRequestId(UUID requestId);
}

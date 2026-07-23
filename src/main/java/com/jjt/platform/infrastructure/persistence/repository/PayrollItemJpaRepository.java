package com.jjt.platform.infrastructure.persistence.repository;

import com.jjt.platform.infrastructure.persistence.entity.PayrollItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PayrollItemJpaRepository extends JpaRepository<PayrollItemEntity, UUID> {
    List<PayrollItemEntity> findByRunId(UUID runId);
}

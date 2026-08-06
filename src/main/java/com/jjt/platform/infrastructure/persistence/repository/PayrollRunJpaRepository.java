package com.jjt.platform.infrastructure.persistence.repository;

import com.jjt.platform.infrastructure.persistence.entity.PayrollRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PayrollRunJpaRepository extends JpaRepository<PayrollRunEntity, UUID> {
    List<PayrollRunEntity> findByOrgIdOrderByCreatedAtDesc(UUID orgId);
    List<PayrollRunEntity> findByOrgIdAndStatusOrderByCreatedAtDesc(UUID orgId, String status);
    List<PayrollRunEntity> findByOrgIdAndPeriodId(UUID orgId, UUID periodId);
}

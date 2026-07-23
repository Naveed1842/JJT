package com.jjt.platform.infrastructure.persistence.repository;

import com.jjt.platform.infrastructure.persistence.entity.FinancialPeriodEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FinancialPeriodJpaRepository extends JpaRepository<FinancialPeriodEntity, UUID> {
    List<FinancialPeriodEntity> findByOrgIdOrderByStartDateDesc(UUID orgId);
    List<FinancialPeriodEntity> findByOrgIdAndStatusOrderByStartDateDesc(UUID orgId, String status);
    Optional<FinancialPeriodEntity> findByOrgIdAndLabel(UUID orgId, String label);
}

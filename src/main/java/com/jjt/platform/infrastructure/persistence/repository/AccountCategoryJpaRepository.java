package com.jjt.platform.infrastructure.persistence.repository;

import com.jjt.platform.infrastructure.persistence.entity.AccountCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AccountCategoryJpaRepository extends JpaRepository<AccountCategoryEntity, Integer> {
    List<AccountCategoryEntity> findByOrgIdIsNullOrOrgId(UUID orgId);
    List<AccountCategoryEntity> findByOrgIdIsNullOrOrgIdAndActive(UUID orgId, boolean active);
}

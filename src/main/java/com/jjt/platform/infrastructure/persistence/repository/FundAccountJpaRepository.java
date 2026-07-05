package com.jjt.platform.infrastructure.persistence.repository;

import com.jjt.platform.infrastructure.persistence.entity.FundAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FundAccountJpaRepository extends JpaRepository<FundAccountEntity, UUID> {
    List<FundAccountEntity> findByOrganisationId(UUID organisationId);
    boolean existsByOrganisationId(UUID organisationId);
}

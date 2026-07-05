package com.jjt.platform.infrastructure.persistence.repository;

import com.jjt.platform.infrastructure.persistence.entity.OrganisationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrganisationJpaRepository extends JpaRepository<OrganisationEntity, UUID> {

    Optional<OrganisationEntity> findBySlug(String slug);
}

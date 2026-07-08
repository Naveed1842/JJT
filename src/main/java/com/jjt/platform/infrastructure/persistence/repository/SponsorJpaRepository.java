package com.jjt.platform.infrastructure.persistence.repository;

import com.jjt.platform.infrastructure.persistence.entity.SponsorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SponsorJpaRepository extends JpaRepository<SponsorEntity, UUID> {

    List<SponsorEntity> findByOrganisationId(UUID organisationId);

    java.util.Optional<SponsorEntity> findByContactEmail(String contactEmail);
}

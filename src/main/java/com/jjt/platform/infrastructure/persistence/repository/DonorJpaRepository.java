package com.jjt.platform.infrastructure.persistence.repository;

import com.jjt.platform.infrastructure.persistence.entity.DonorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DonorJpaRepository extends JpaRepository<DonorEntity, UUID> {

    List<DonorEntity> findByOrganisationIdOrderByDisplayNameAsc(UUID organisationId);

    Optional<DonorEntity> findByEmailAndOrganisationId(String email, UUID organisationId);
}

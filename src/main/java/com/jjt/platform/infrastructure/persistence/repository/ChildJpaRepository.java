package com.jjt.platform.infrastructure.persistence.repository;

import com.jjt.platform.infrastructure.persistence.entity.ChildEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChildJpaRepository extends JpaRepository<ChildEntity, UUID> {

    List<ChildEntity> findByOrganisationId(UUID organisationId);
}

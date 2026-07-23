package com.jjt.platform.infrastructure.persistence.repository;

import com.jjt.platform.infrastructure.persistence.entity.PersonEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PersonJpaRepository extends JpaRepository<PersonEntity, UUID> {
    List<PersonEntity> findByOrgIdOrderByLastNameAsc(UUID orgId);
    List<PersonEntity> findByOrgIdAndActiveOrderByLastNameAsc(UUID orgId, boolean active);
    List<PersonEntity> findByOrgIdAndKindAndActive(UUID orgId, String kind, boolean active);
}

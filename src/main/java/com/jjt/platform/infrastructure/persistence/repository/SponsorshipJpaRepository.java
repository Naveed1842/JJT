package com.jjt.platform.infrastructure.persistence.repository;

import com.jjt.platform.infrastructure.persistence.entity.SponsorshipEntity;
import com.jjt.platform.core.domain.entity.SponsorshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SponsorshipJpaRepository extends JpaRepository<SponsorshipEntity, UUID> {

    List<SponsorshipEntity> findByStatus(SponsorshipStatus status);

    boolean existsByChildIdAndStatus(UUID childId, SponsorshipStatus status);

    List<SponsorshipEntity> findByChildIdOrderByCreatedAtDesc(UUID childId);
}

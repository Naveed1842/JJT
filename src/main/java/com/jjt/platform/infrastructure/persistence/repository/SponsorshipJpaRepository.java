package com.jjt.platform.infrastructure.persistence.repository;

import com.jjt.platform.infrastructure.persistence.entity.SponsorshipEntity;
import com.jjt.platform.core.domain.entity.SponsorshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface SponsorshipJpaRepository extends JpaRepository<SponsorshipEntity, UUID> {

    List<SponsorshipEntity> findByStatus(SponsorshipStatus status);

    List<SponsorshipEntity> findByOrganisationIdAndStatus(UUID organisationId, SponsorshipStatus status);

    List<SponsorshipEntity> findByOrganisationId(UUID organisationId);

    boolean existsByChildIdAndStatus(UUID childId, SponsorshipStatus status);

    boolean existsByChildIdAndStatusIn(UUID childId, java.util.Collection<SponsorshipStatus> statuses);

    List<SponsorshipEntity> findByChildIdOrderByCreatedAtDesc(UUID childId);

    List<SponsorshipEntity> findBySponsor_Id(UUID sponsorId);

    boolean existsBySponsor_IdAndChildId(UUID sponsorId, UUID childId);

    @Query("SELECT s.childId FROM SponsorshipEntity s WHERE s.status = :status")
    Set<UUID> findChildIdsByStatus(@Param("status") SponsorshipStatus status);
}

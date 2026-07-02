package com.jjt.platform.infrastructure.persistence.repository;

import com.jjt.platform.infrastructure.persistence.entity.CampaignEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CampaignJpaRepository extends JpaRepository<CampaignEntity, UUID> {

    List<CampaignEntity> findByOrganisationId(UUID orgId);

    List<CampaignEntity> findByOrganisationIdAndStatus(UUID orgId, String status);

    List<CampaignEntity> findByStatus(String status);
}

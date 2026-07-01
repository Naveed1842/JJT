package com.jjt.platform.infrastructure.persistence.repository;

import com.jjt.platform.core.domain.entity.DonationStatus;
import com.jjt.platform.infrastructure.persistence.entity.DonationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DonationJpaRepository extends JpaRepository<DonationEntity, UUID> {

    Page<DonationEntity> findByOrganisationId(UUID organisationId, Pageable pageable);

    List<DonationEntity> findByOrganisationIdAndDonorId(UUID organisationId, UUID donorId);

    List<DonationEntity> findByOrganisationIdAndStatus(UUID organisationId, DonationStatus status);

    List<DonationEntity> findByRecurringScheduleIdAndStatus(UUID recurringScheduleId, DonationStatus status);
}

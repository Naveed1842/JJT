package com.jjt.platform.infrastructure.persistence.repository;

import com.jjt.platform.core.domain.entity.RecurringDonationStatus;
import com.jjt.platform.infrastructure.persistence.entity.RecurringDonationScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface RecurringDonationScheduleJpaRepository extends JpaRepository<RecurringDonationScheduleEntity, UUID> {

    List<RecurringDonationScheduleEntity> findByOrganisationIdAndStatus(
            UUID organisationId, RecurringDonationStatus status);

    List<RecurringDonationScheduleEntity> findByStatusAndNextDueDateLessThanEqual(
            RecurringDonationStatus status, LocalDate date);

    List<RecurringDonationScheduleEntity> findByOrganisationId(UUID organisationId);
}
